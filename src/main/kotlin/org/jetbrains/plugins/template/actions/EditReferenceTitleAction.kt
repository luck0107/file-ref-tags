package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.utils.NotificationUtils
import org.jetbrains.plugins.template.model.ReferenceItem
import org.jetbrains.plugins.template.services.ReferenceDataService

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
