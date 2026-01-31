package com.github.luck0107.filereftags

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.github.luck0107.filereftags.MyBundle
import com.github.luck0107.filereftags.utils.NotificationUtils
import com.github.luck0107.filereftags.utils.ReferenceNavigationUtils
import java.net.URI
import java.net.URLDecoder

/**
 * URI Handler for jetbrains:// and vscode:// protocols
 */
object UriHandler {
    private val logger = thisLogger()

    /**
     * 处理 URI
     */
    fun handleUri(uriString: String) {
        try {
            val uri = URI(uriString)
            val scheme = uri.scheme
            
            // 支持 jetbrains:// 和 vscode:// 协议
            if (scheme != "jetbrains" && scheme != "vscode") {
                logger.warn("Unsupported URI scheme: $scheme")
                return
            }

            // 解析查询参数
            val query = uri.query ?: ""
            val params = parseQueryString(query)
            
            val filePath = params["filePath"]?.let { URLDecoder.decode(it, "UTF-8") }
            val snippet = params["snippet"]?.let { URLDecoder.decode(it, "UTF-8") }

            // 确保至少有一个参数
            if (filePath == null && snippet == null) {
                logger.warn("URI missing required parameters: filePath or snippet")
                showError(MyBundle.message("uriHandler.missingParams"))
                return
            }

            // 获取当前项目或第一个打开的项目
            val project = getCurrentProject()
            if (project == null) {
                showError(MyBundle.message("uriHandler.noProject"))
                return
            }

            // 根据参数组合决定跳转模式
            ApplicationManager.getApplication().invokeLater {
                when {
                    filePath != null && snippet != null -> {
                        // 模式1：file-snippet，跳转到文件并搜索代码片段
                        ReferenceNavigationUtils.jumpToFileAndSnippet(project, filePath, snippet)
                    }
                    filePath != null -> {
                        // 模式2：file，直接跳转到文件
                        ReferenceNavigationUtils.jumpToFile(project, filePath)
                    }
                    snippet != null -> {
                        // 模式3：global-snippet，全局搜索代码片段
                        ReferenceNavigationUtils.jumpToGlobalSnippet(project, snippet)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to handle URI: $uriString", e)
            showError("处理URL失败：${e.message}")
        }
    }

    /**
     * 解析查询字符串
     */
    private fun parseQueryString(query: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        if (query.isEmpty()) return params

        query.split("&").forEach { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                params[parts[0]] = parts[1]
            }
        }
        return params
    }

    /**
     * 获取当前项目
     */
    private fun getCurrentProject(): Project? {
        val projectManager = ProjectManager.getInstance()
        val openProjects = projectManager.openProjects
        return openProjects.firstOrNull() ?: projectManager.defaultProject
    }

    /**
     * 显示错误消息
     */
    private fun showError(message: String) {
        ApplicationManager.getApplication().invokeLater {
            val project = getCurrentProject()
            if (project != null) {
                NotificationUtils.showError(
                    project,
                    MyBundle.message("uriHandler.errorTitle"),
                    message
                )
            }
        }
    }
}
