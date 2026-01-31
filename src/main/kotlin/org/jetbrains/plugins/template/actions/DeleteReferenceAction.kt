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
 * 删除引用项
 */
class DeleteReferenceAction(
    private val reference: ReferenceItem,
    private val onDeleted: () -> Unit
) : AnAction(
    MyBundle.message("action.deleteReference.text"),
    MyBundle.message("action.deleteReference.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dataService = project.service<ReferenceDataService>()

        val result = Messages.showYesNoDialog(
            project,
            MyBundle.message("action.deleteReference.confirm", reference.title),
            MyBundle.message("action.deleteReference.title"),
            Messages.getQuestionIcon()
        )

        if (result == Messages.YES) {
            dataService.deleteReference(reference.id)
            onDeleted()
            
            NotificationUtils.showInfo(
                project,
                MyBundle.message("action.deleteReference.title"),
                MyBundle.message("action.deleteReference.success")
            )
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
