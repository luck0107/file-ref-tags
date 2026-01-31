package org.jetbrains.plugins.template.toolWindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerListener
import org.jetbrains.plugins.template.services.ReferenceDataService
import org.jetbrains.plugins.template.ui.ReferenceListPanel

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // 获取服务（不立即加载数据）
        val dataService = project.getService(ReferenceDataService::class.java)
        
        val panel = ReferenceListPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
        
        // 添加标题栏动作（刷新按钮）
        val refreshAction = ActionManager.getInstance().getAction("FileRefTags.TitleActions.Refresh")
        if (refreshAction != null) {
            toolWindow.setTitleActions(listOf(refreshAction))
        }
        
        // 监听工具窗口显示事件，在显示时初始化并加载数据
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun selectionChanged(event: com.intellij.ui.content.ContentManagerEvent) {
                // 当工具窗口内容被选中显示时，初始化并加载数据
                if (event.content.component is ReferenceListPanel) {
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        // 初始化数据服务（延迟加载）
                        dataService.initialize()
                        // 刷新面板显示
                        (event.content.component as ReferenceListPanel).loadReferences()
                    }
                }
            }
        })
    }

    override fun shouldBeAvailable(project: Project) = true
    
    override fun init(toolWindow: ToolWindow) {
        // 工具窗口初始化时，只获取服务实例，不立即加载数据
        val project = toolWindow.project
        if (project != null) {
            // 获取服务实例（延迟加载数据）
            project.getService(ReferenceDataService::class.java)
        }
    }
}
