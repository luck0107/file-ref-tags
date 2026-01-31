package com.github.luck0107.filereftags.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.utils.NotificationUtils
import com.github.luck0107.filereftags.services.ReferenceDataService

/**
 * 刷新引用列表
 */
class RefreshReferencesAction : AnAction(
    MyBundle.message("action.refreshReferences.text"),
    MyBundle.message("action.refreshReferences.description"),
    AllIcons.Actions.Refresh
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dataService = project.service<ReferenceDataService>()
        
        // 重新加载数据（文件 I/O 操作）
        dataService.reloadReferences()
        
        // 获取加载后的数据数量
        val referencesCount = dataService.getReferences().size
        val groupsCount = dataService.getGroups().size
        
        // 在 UI 线程刷新工具窗口
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            refreshToolWindow(project)
            
            // 显示刷新结果提示
            if (referencesCount > 0 || groupsCount > 0) {
                NotificationUtils.showInfo(
                    project,
                    "刷新完成",
                    "已刷新：${referencesCount} 个引用，${groupsCount} 个分组"
                )
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
