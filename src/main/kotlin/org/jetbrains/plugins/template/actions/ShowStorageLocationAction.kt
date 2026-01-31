package org.jetbrains.plugins.template.actions

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.utils.NotificationUtils
import org.jetbrains.plugins.template.services.ReferenceDataService
import java.io.File

/**
 * 显示存储位置
 */
class ShowStorageLocationAction : AnAction(
    MyBundle.message("action.showStorageLocation.text"),
    MyBundle.message("action.showStorageLocation.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dataService = project.service<ReferenceDataService>()
        
        val storagePath = dataService.getStoragePath()
        val storageFile = File(storagePath)
        
        if (!storageFile.exists()) {
            NotificationUtils.showWarning(
                project,
                MyBundle.message("action.showStorageLocation.title"),
                MyBundle.message("action.showStorageLocation.notFound", storagePath)
            )
            return
        }

        // 在文件管理器中显示文件
        RevealFileAction.openFile(storageFile)
        
        // 同时在编辑器中打开文件
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(storagePath)
        if (virtualFile != null) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
        }
        
        NotificationUtils.showInfo(
            project,
            MyBundle.message("action.showStorageLocation.title"),
            MyBundle.message("action.showStorageLocation.success", storagePath)
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
