package com.github.luck0107.filereftags.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.utils.NotificationUtils
import com.github.luck0107.filereftags.model.ReferenceGroup
import com.github.luck0107.filereftags.services.ReferenceDataService
import java.time.Instant
import java.util.*

/**
 * 添加分组
 */
class AddGroupAction : AnAction(
    MyBundle.message("action.addGroup.text"),
    MyBundle.message("action.addGroup.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // 显示输入对话框
        val groupName = Messages.showInputDialog(
            project,
            MyBundle.message("action.addGroup.prompt"),
            MyBundle.message("action.addGroup.title"),
            Messages.getQuestionIcon(),
            "",
            null
        ) ?: return

        if (groupName.isBlank()) {
            NotificationUtils.showWarning(
                project,
                MyBundle.message("action.addGroup.title"),
                MyBundle.message("action.addGroup.empty")
            )
            return
        }

        val dataService = project.service<ReferenceDataService>()
        val now = Instant.now().toString()

        val group = ReferenceGroup(
            id = "group-${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 9)}",
            name = groupName,
            createdAt = now,
            updatedAt = now
        )

        dataService.addGroup(group)
        refreshToolWindow(project)

        NotificationUtils.showInfo(
            project,
            MyBundle.message("action.addGroup.title"),
            MyBundle.message("action.addGroup.success", groupName)
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
