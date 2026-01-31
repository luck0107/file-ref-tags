package com.github.luck0107.filereftags.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.utils.NotificationUtils
import com.github.luck0107.filereftags.model.ReferenceItem
import com.github.luck0107.filereftags.services.ReferenceDataService

/**
 * 编辑引用项标题
 */
class EditReferenceTitleAction(
    private val reference: ReferenceItem,
    private val onUpdated: () -> Unit
) : AnAction(
    MyBundle.message("action.editReferenceTitle.text"),
    MyBundle.message("action.editReferenceTitle.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dataService = project.service<ReferenceDataService>()

        // 显示输入对话框
        val newTitle = Messages.showInputDialog(
            project,
            MyBundle.message("action.editReferenceTitle.prompt"),
            MyBundle.message("action.editReferenceTitle.title"),
            Messages.getQuestionIcon(),
            reference.title,
            null
        ) ?: return

        if (newTitle.isBlank()) {
            NotificationUtils.showWarning(
                project,
                MyBundle.message("action.editReferenceTitle.title"),
                MyBundle.message("action.editReferenceTitle.empty")
            )
            return
        }

        dataService.updateReferenceTitle(reference.id, newTitle)
        onUpdated()
        
        NotificationUtils.showInfo(
            project,
            MyBundle.message("action.editReferenceTitle.title"),
            MyBundle.message("action.editReferenceTitle.success")
        )
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
