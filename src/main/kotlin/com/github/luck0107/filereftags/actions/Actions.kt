package com.github.luck0107.filereftags.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

/**
 * 刷新工具窗口
 */
fun refreshToolWindow(project: Project) {
    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("FileRefTags")
    toolWindow?.contentManager?.contents?.forEach { content ->
        val component = content.component
        if (component is com.github.luck0107.filereftags.ui.ReferenceListPanel) {
            component.loadReferences()
        }
    }
}
