package com.github.luck0107.filereftags.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.ProjectScope
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.utils.NotificationUtils
import com.github.luck0107.filereftags.model.ReferenceItem
import com.github.luck0107.filereftags.model.ReferenceType
import com.github.luck0107.filereftags.services.ReferenceDataService
import java.time.Instant
import java.util.*

/**
 * 添加全局唯一片段到面板
 */
class AddGlobalUniqueSnippetAction : AnAction(
    MyBundle.message("action.addGlobalUniqueSnippet.text"),
    MyBundle.message("action.addGlobalUniqueSnippet.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) {
            NotificationUtils.showWarning(
                project,
                MyBundle.message("action.addGlobalUniqueSnippet.title"),
                MyBundle.message("action.addGlobalUniqueSnippet.noSelection")
            )
            return
        }

        val snippet = selectionModel.selectedText ?: ""
        val filePath = virtualFile.path

        // 在后台线程中检查代码片段是否全局唯一
        ApplicationManager.getApplication().executeOnPooledThread {
            val matchCount = countSnippetMatches(project, snippet)
            
            ApplicationManager.getApplication().invokeLater {
                when {
                    matchCount == 0 -> {
                        NotificationUtils.showError(
                            project,
                            MyBundle.message("action.addGlobalUniqueSnippet.title"),
                            MyBundle.message("action.addGlobalUniqueSnippet.notFound")
                        )
                    }
                    matchCount == 1 -> {
                        // 确认是全局唯一的，直接添加到面板
                        val dataService = project.service<ReferenceDataService>()
                        val now = Instant.now().toString()
                        
                        val snippetPreview = if (snippet.length > 50) snippet.substring(0, 50) + "..." else snippet
                        val title = "Global: $snippetPreview"
                        
                        val reference = ReferenceItem(
                            id = "ref-${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 9)}",
                            type = ReferenceType.GLOBAL_SNIPPET,
                            title = title,
                            snippet = snippet,
                            targetFilePath = filePath,
                            createdAt = now,
                            updatedAt = now
                        )
                        
                        dataService.addReference(reference)
                        refreshToolWindow(project)
                        
                        NotificationUtils.showInfo(
                            project,
                            MyBundle.message("action.addGlobalUniqueSnippet.title"),
                            MyBundle.message("action.addGlobalUniqueSnippet.success")
                        )
                    }
                    else -> {
                        // 不是全局唯一的，告诉用户
                        NotificationUtils.showWarning(
                            project,
                            MyBundle.message("action.addGlobalUniqueSnippet.title"),
                            MyBundle.message("action.addGlobalUniqueSnippet.notUnique", matchCount)
                        )
                    }
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = project != null && editor != null && editor.selectionModel.hasSelection()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    /**
     * 统计代码片段在项目中的匹配数量
     */
    private fun countSnippetMatches(project: Project, snippet: String): Int {
        val psiManager = PsiManager.getInstance(project)
        val searchScope = ProjectScope.getContentScope(project)
        
        var matchCount = 0
        val baseDir = project.baseDir ?: return 0
        
        // 收集所有文件
        val allFiles = mutableListOf<com.intellij.openapi.vfs.VirtualFile>()
        collectVirtualFiles(baseDir, allFiles)
        
        // 遍历文件，查找包含代码片段的文件
        for (file in allFiles) {
            try {
                val psiFile = psiManager.findFile(file) ?: continue
                val text = psiFile.text
                if (text.contains(snippet)) {
                    matchCount++
                    // 如果超过1个匹配，就可以提前结束
                    if (matchCount > 1) {
                        break
                    }
                }
            } catch (e: Exception) {
                // 忽略无法打开的文件
                continue
            }
        }
        
        return matchCount
    }

    /**
     * 递归收集虚拟文件
     */
    private fun collectVirtualFiles(virtualFile: com.intellij.openapi.vfs.VirtualFile, result: MutableList<com.intellij.openapi.vfs.VirtualFile>) {
        if (virtualFile.isDirectory) {
            // 跳过隐藏目录和常见排除目录
            if (virtualFile.name.startsWith(".") || 
                virtualFile.name == "node_modules" || 
                virtualFile.name == ".idea" ||
                virtualFile.name == "build" ||
                virtualFile.name == "out") {
                return
            }
            virtualFile.children.forEach { collectVirtualFiles(it, result) }
        } else {
            result.add(virtualFile)
        }
    }
}
