package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.utils.NotificationUtils
import org.jetbrains.plugins.template.model.ReferenceItem
import org.jetbrains.plugins.template.model.ReferenceType
import org.jetbrains.plugins.template.services.ReferenceDataService
import java.io.File
import java.time.Instant
import java.util.*

/**
 * 添加当前文件+选中的片段到面板
 */
class AddFileAndSnippetAction : AnAction(
    MyBundle.message("action.addFileAndSnippet.text"),
    MyBundle.message("action.addFileAndSnippet.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val selectionModel = editor.selectionModel
        if (selectionModel.selectedText.isNullOrBlank()) {
            NotificationUtils.showWarning(
                project,
                MyBundle.message("action.addFileAndSnippet.title"),
                MyBundle.message("action.addFileAndSnippet.noSelection")
            )
            return
        }

        val filePath = virtualFile.path
        val fileName = File(filePath).name
        val snippet = selectionModel.selectedText ?: ""

        val dataService = project.service<ReferenceDataService>()
        val now = Instant.now().toString()
        
        val snippetPreview = if (snippet.length > 50) snippet.substring(0, 50) + "..." else snippet
        val title = "$fileName: $snippetPreview"

        val reference = ReferenceItem(
            id = "ref-${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 9)}",
            type = ReferenceType.FILE_SNIPPET,
            title = title,
            filePath = filePath,
            snippet = snippet,
            createdAt = now,
            updatedAt = now
        )

        dataService.addReference(reference)
        
        // 刷新工具窗口
        refreshToolWindow(project)
        
        NotificationUtils.showInfo(
            project,
            MyBundle.message("action.addFileAndSnippet.title"),
            MyBundle.message("action.addFileAndSnippet.success")
        )
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        e.presentation.isEnabled = e.project != null && editor != null && hasSelection
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
