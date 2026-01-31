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
import org.jetbrains.plugins.template.model.ReferenceType
import org.jetbrains.plugins.template.services.ReferenceDataService
import java.time.Instant
import java.util.*

/**
 * 添加用户注释到面板
 */
class AddCommentAction : AnAction(
    MyBundle.message("action.addComment.text"),
    MyBundle.message("action.addComment.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // 显示输入对话框
        val comment = Messages.showInputDialog(
            project,
            MyBundle.message("action.addComment.prompt"),
            MyBundle.message("action.addComment.title"),
            Messages.getQuestionIcon(),
            "",
            null
        ) ?: return

        if (comment.isBlank()) {
            NotificationUtils.showWarning(
                project,
                MyBundle.message("action.addComment.title"),
                MyBundle.message("action.addComment.empty")
            )
            return
        }

        val dataService = project.service<ReferenceDataService>()
        val now = Instant.now().toString()

        val reference = ReferenceItem(
            id = "ref-${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 9)}",
            type = ReferenceType.COMMENT,
            title = if (comment.length > 50) comment.substring(0, 50) + "..." else comment,
            comment = comment,
            createdAt = now,
            updatedAt = now
        )

        dataService.addReference(reference)
        refreshToolWindow(project)

        NotificationUtils.showInfo(
            project,
            MyBundle.message("action.addComment.title"),
            MyBundle.message("action.addComment.success")
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
