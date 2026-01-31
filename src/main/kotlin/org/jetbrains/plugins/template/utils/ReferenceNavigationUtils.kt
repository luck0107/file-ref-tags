package org.jetbrains.plugins.template.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.template.utils.NotificationUtils
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.model.ReferenceItem
import org.jetbrains.plugins.template.model.ReferenceType
import org.jetbrains.plugins.template.services.ReferenceDataService
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 引用导航工具类
 * 负责处理引用项的跳转逻辑
 */
object ReferenceNavigationUtils {

    /**
     * 跳转到引用项
     * 
     * @param project 当前项目
     * @param reference 要跳转的引用项
     */
    fun jumpToReference(project: Project, reference: ReferenceItem) {
        when (reference.type) {
            ReferenceType.FILE -> {
                // 直接跳转到文件
                jumpToFile(project, reference.filePath)
            }
            ReferenceType.FILE_SNIPPET -> {
                // 跳转到文件并搜索代码片段
                if (reference.filePath != null && reference.snippet != null) {
                    jumpToFileAndSnippet(project, reference.filePath, reference.snippet)
                }
            }
            ReferenceType.GLOBAL_SNIPPET -> {
                // 全局搜索代码片段
                if (reference.snippet != null) {
                    jumpToGlobalSnippet(project, reference)
                }
            }
            ReferenceType.COMMENT -> {
                // 注释项，无跳转功能
                NotificationUtils.showInfo(
                    project,
                    MyBundle.message("navigation.comment.title"),
                    MyBundle.message("navigation.comment.noJump")
                )
            }
        }
    }

    /**
     * 跳转到文件
     * 
     * @param project 当前项目
     * @param filePath 文件路径（可以是绝对路径、相对路径或仅文件名）
     */
    fun jumpToFile(project: Project, filePath: String?) {
        if (filePath == null) {
            NotificationUtils.showError(
                project,
                MyBundle.message("navigation.error.title"),
                MyBundle.message("navigation.file.noPath")
            )
            return
        }

        val virtualFile = findFile(project, filePath)
        if (virtualFile != null) {
            openFile(project, virtualFile)
        } else {
            NotificationUtils.showError(
                project,
                MyBundle.message("navigation.error.title"),
                MyBundle.message("navigation.file.notFound", filePath)
            )
        }
    }

    /**
     * 跳转到文件并搜索代码片段
     * 
     * @param project 当前项目
     * @param filePath 文件路径
     * @param snippet 代码片段
     */
    fun jumpToFileAndSnippet(project: Project, filePath: String, snippet: String) {
        val virtualFile = findFile(project, filePath)
        if (virtualFile == null) {
            NotificationUtils.showError(
                project,
                MyBundle.message("navigation.error.title"),
                MyBundle.message("navigation.file.notFound", filePath)
            )
            return
        }

        val editor = openFile(project, virtualFile) ?: return

        // 在文件中搜索代码片段
        ApplicationManager.getApplication().invokeLater {
            searchAndSelectSnippet(project, editor, snippet) { found ->
                if (!found) {
                    NotificationUtils.showWarning(
                        project,
                        MyBundle.message("navigation.warning.title"),
                        MyBundle.message("navigation.snippet.notFoundInFile", snippet)
                    )
                }
            }
        }
    }

    /**
     * 跳转到全局代码片段（仅代码片段，不使用引用项）
     * 
     * @param project 当前项目
     * @param snippet 代码片段
     */
    fun jumpToGlobalSnippet(project: Project, snippet: String) {
        val dataService = project.service<ReferenceDataService>()
        
        // 创建临时引用项用于跳转
        val tempReference = ReferenceItem(
            id = "temp",
            type = ReferenceType.GLOBAL_SNIPPET,
            title = "Temp",
            snippet = snippet,
            createdAt = "",
            updatedAt = ""
        )
        
        jumpToGlobalSnippet(project, tempReference)
    }

    /**
     * 跳转到全局代码片段（使用层级搜索算法）
     * 
     * @param project 当前项目
     * @param reference 引用项
     */
    private fun jumpToGlobalSnippet(project: Project, reference: ReferenceItem) {
        val snippet = reference.snippet ?: return
        val dataService = project.service<ReferenceDataService>()

        // 第一步：尝试在记录的文件路径中查找
        if (reference.targetFilePath != null) {
            val found = searchAndJumpInFile(project, reference.targetFilePath, snippet)
            if (found) {
                return
            }
        }

        // 第二步：如果在记录的文件中没有找到，则使用层级搜索
        if (reference.targetFilePath != null) {
            val found = hierarchicalSearchAndJump(project, reference.targetFilePath, snippet, reference.id, dataService)
            if (found) {
                return
            }
        }

        // 第三步：如果层级搜索没有找到，则进行全局搜索
        globalSearchAndJump(project, reference, dataService)
    }

    /**
     * 在指定文件中搜索代码片段并跳转
     * 
     * @param project 当前项目
     * @param filePath 文件路径
     * @param snippet 代码片段
     * @return 是否找到并跳转成功
     */
    private fun searchAndJumpInFile(project: Project, filePath: String, snippet: String): Boolean {
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return false
        val editor = openFile(project, virtualFile) ?: return false

        var found = false
        ApplicationManager.getApplication().invokeAndWait {
            found = searchAndSelectSnippet(project, editor, snippet) { }
        }
        return found
    }

    /**
     * 层级搜索并在找到后跳转
     * 
     * @param project 当前项目
     * @param originalFilePath 原始文件路径
     * @param snippet 代码片段
     * @param referenceId 引用项ID（用于更新目标文件路径）
     * @param dataService 数据服务
     * @return 是否找到并跳转成功
     */
    private fun hierarchicalSearchAndJump(
        project: Project,
        originalFilePath: String,
        snippet: String,
        referenceId: String,
        dataService: ReferenceDataService
    ): Boolean {
        val originalFile = File(originalFilePath)
        val originalDir = originalFile.parentFile ?: return false

        // 首先在原始文件所在目录搜索
        val foundInCurrentDir = searchInDirectory(project, originalDir.toPath(), snippet, referenceId, dataService)
        if (foundInCurrentDir) {
            return true
        }

        // 如果在当前目录没找到，向上递归搜索父级目录
        var parentDir = originalDir.parentFile
        val rootPath = Paths.get(originalDir.absolutePath).root?.toFile()

        while (parentDir != null && parentDir != rootPath && parentDir.absolutePath != originalDir.absolutePath) {
            val foundInParentDir = searchInDirectory(project, parentDir.toPath(), snippet, referenceId, dataService)
            if (foundInParentDir) {
                return true
            }

            val nextParentDir = parentDir.parentFile
            if (nextParentDir == parentDir) {
                // 已经到达根目录
                break
            }
            parentDir = nextParentDir
        }

        return false
    }

    /**
     * 在指定目录中搜索代码片段
     * 
     * @param project 当前项目
     * @param directory 目录路径
     * @param snippet 代码片段
     * @param referenceId 引用项ID
     * @param dataService 数据服务
     * @return 是否找到并跳转成功
     */
    private fun searchInDirectory(
        project: Project,
        directory: Path,
        snippet: String,
        referenceId: String,
        dataService: ReferenceDataService
    ): Boolean {
        val psiManager = PsiManager.getInstance(project)
        val searchScope = ProjectScope.getContentScope(project)

        // 获取目录下的所有文件
        val directoryFile = directory.toFile()
        if (!directoryFile.exists() || !directoryFile.isDirectory) {
            return false
        }

        // 递归搜索目录下的所有文件
        directoryFile.walkTopDown()
            .filter { it.isFile && !it.name.startsWith(".") }
            .forEach { file ->
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(file.absolutePath)
                if (virtualFile != null) {
                    val found = searchAndJumpInFile(project, file.absolutePath, snippet)
                    if (found) {
                        // 更新记录的文件路径
                        dataService.updateReferenceTargetFilePath(referenceId, file.absolutePath)
                        return true
                    }
                }
            }

        return false
    }

    /**
     * 全局搜索并在找到后跳转
     * 
     * @param project 当前项目
     * @param reference 引用项
     * @param dataService 数据服务
     */
    private fun globalSearchAndJump(
        project: Project,
        reference: ReferenceItem,
        dataService: ReferenceDataService
    ) {
        val snippet = reference.snippet ?: return

        ApplicationManager.getApplication().executeOnPooledThread {
            val psiManager = PsiManager.getInstance(project)
            val searchScope = ProjectScope.getContentScope(project)

            // 获取项目中的所有文件
            val allFiles = mutableListOf<VirtualFile>()
            val baseDir = project.baseDir
            if (baseDir != null) {
                collectVirtualFiles(baseDir, allFiles)
            }

            var matchCount = 0
            var matchFile: VirtualFile? = null
            var matchStartOffset: Int = -1
            var matchEndOffset: Int = -1

            // 遍历文件，查找包含代码片段的文件
            for (file in allFiles) {
                try {
                    val psiFile = psiManager.findFile(file) ?: continue
                    val text = psiFile.text
                    val index = text.indexOf(snippet)
                    if (index != -1) {
                        matchCount++
                        matchFile = file
                        matchStartOffset = index
                        matchEndOffset = index + snippet.length
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

            ApplicationManager.getApplication().invokeLater {
                when {
                    matchCount == 0 -> {
                        NotificationUtils.showWarning(
                            project,
                            MyBundle.message("navigation.warning.title"),
                            MyBundle.message("navigation.snippet.notFound")
                        )
                    }
                    matchCount == 1 && matchFile != null -> {
                        val editor = openFile(project, matchFile)
                        if (editor != null && matchStartOffset >= 0) {
                            ApplicationManager.getApplication().invokeLater {
                                val document = editor.document
                                val startLine = document.getLineNumber(matchStartOffset)
                                val endLine = document.getLineNumber(matchEndOffset)
                                val startColumn = matchStartOffset - document.getLineStartOffset(startLine)
                                val endColumn = matchEndOffset - document.getLineStartOffset(endLine)

                                val startOffset = document.getLineStartOffset(startLine) + startColumn
                                val endOffset = document.getLineStartOffset(endLine) + endColumn

                                editor.selectionModel.setSelection(startOffset, endOffset)
                                editor.caretModel.moveToOffset(startOffset)
                                editor.scrollingModel.scrollToCaret(ScrollType.CENTER)

                                // 更新记录的文件路径
                                if (reference.targetFilePath != matchFile.path) {
                                    dataService.updateReferenceTargetFilePath(reference.id, matchFile.path)
                                }
                            }
                        }
                    }
                    else -> {
                        NotificationUtils.showWarning(
                            project,
                            MyBundle.message("navigation.warning.title"),
                            MyBundle.message("navigation.snippet.notUnique")
                        )
                    }
                }
            }
        }
    }

    /**
     * 递归收集虚拟文件
     */
    private fun collectVirtualFiles(virtualFile: VirtualFile, result: MutableList<VirtualFile>) {
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

    /**
     * 查找文件（支持绝对路径、相对路径和仅文件名）
     * 
     * @param project 当前项目
     * @param filePath 文件路径
     * @return 虚拟文件，如果未找到则返回 null
     */
    private fun findFile(project: Project, filePath: String): VirtualFile? {
        // 首先尝试作为绝对路径
        val absoluteFile = File(filePath)
        if (absoluteFile.isAbsolute && absoluteFile.exists()) {
            return LocalFileSystem.getInstance().findFileByPath(absoluteFile.absolutePath)
        }

        // 尝试作为项目相对路径
        val baseDir = project.baseDir ?: return null
        val relativeFile = File(baseDir.path, filePath)
        if (relativeFile.exists()) {
            return LocalFileSystem.getInstance().findFileByPath(relativeFile.absolutePath)
        }

        // 尝试仅文件名搜索
        val psiManager = PsiManager.getInstance(project)
        val searchScope = ProjectScope.getContentScope(project)
        val files = FilenameIndex.getFilesByName(project, absoluteFile.name, searchScope)
        
        return files.firstOrNull()?.virtualFile
    }

    /**
     * 打开文件并返回编辑器
     * 
     * @param project 当前项目
     * @param virtualFile 虚拟文件
     * @return 编辑器，如果打开失败则返回 null
     */
    private fun openFile(project: Project, virtualFile: VirtualFile): Editor? {
        val descriptor = OpenFileDescriptor(project, virtualFile)
        return FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
    }

    /**
     * 在编辑器中搜索并选中代码片段
     * 
     * @param project 当前项目
     * @param editor 编辑器
     * @param snippet 代码片段
     * @param onComplete 完成回调（参数表示是否找到）
     * @return 是否找到
     */
    private fun searchAndSelectSnippet(
        project: Project,
        editor: Editor,
        snippet: String,
        onComplete: (Boolean) -> Unit
    ): Boolean {
        val document = editor.document
        val text = document.text
        val index = text.indexOf(snippet)

        if (index == -1) {
            onComplete(false)
            return false
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val startLine = document.getLineNumber(index)
            val endLine = document.getLineNumber(index + snippet.length)
            val startColumn = index - document.getLineStartOffset(startLine)
            val endColumn = (index + snippet.length) - document.getLineStartOffset(endLine)

            val startOffset = document.getLineStartOffset(startLine) + startColumn
            val endOffset = document.getLineStartOffset(endLine) + endColumn

            editor.selectionModel.setSelection(startOffset, endOffset)
            editor.caretModel.moveToOffset(startOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }

        onComplete(true)
        return true
    }
}
