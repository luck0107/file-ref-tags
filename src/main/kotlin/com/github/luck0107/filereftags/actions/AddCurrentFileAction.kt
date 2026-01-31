package com.github.luck0107.filereftags.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.utils.NotificationUtils
import com.github.luck0107.filereftags.model.ReferenceItem
import com.github.luck0107.filereftags.model.ReferenceType
import com.github.luck0107.filereftags.services.ReferenceDataService
import java.io.File
import java.time.Instant
import java.util.*

/**
 * 添加当前文件到面板
 */
class AddCurrentFileAction : AnAction(
    MyBundle.message("action.addCurrentFile.text"),
    MyBundle.message("action.addCurrentFile.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val filePath = virtualFile.path
        val fileName = File(filePath).name

        val dataService = project.service<ReferenceDataService>()
        val now = Instant.now().toString()
        
        val reference = ReferenceItem(
            id = "ref-${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 9)}",
            type = ReferenceType.FILE,
            title = fileName,
            filePath = filePath,
            createdAt = now,
            updatedAt = now
        )

        dataService.addReference(reference)
        
        // 刷新工具窗口
        refreshToolWindow(project)
        
        NotificationUtils.showInfo(
            project,
            MyBundle.message("action.addCurrentFile.title"),
            MyBundle.message("action.addCurrentFile.success")
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null && e.getData(CommonDataKeys.VIRTUAL_FILE) != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
