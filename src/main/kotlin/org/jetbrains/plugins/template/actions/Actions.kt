package org.jetbrains.plugins.template.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

/**
 * 刷新工具窗口
 */
fun refreshToolWindow(project: Project) {
    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("FileRefTags")
    toolWindow?.contentManager?.contents?.forEach { content ->
        val component = content.component
        if (component is org.jetbrains.plugins.template.ui.ReferenceListPanel) {
            component.loadReferences()
        }
    }
}
