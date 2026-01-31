package org.jetbrains.plugins.template.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

/**
 * 通知工具类
 * 用于在右下角显示通知消息
 */
object NotificationUtils {
    
    private const val NOTIFICATION_GROUP_ID = "FileRefTags.NotificationGroup"
    
    /**
     * 获取通知组
     */
    private fun getNotificationGroup() = 
        NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
    
    /**
     * 显示信息通知
     * 使用平台默认的自动消失时间（约 5 秒）
     */
    fun showInfo(project: Project?, title: String, content: String) {
        val group = getNotificationGroup()
        val notification = if (group != null) {
            group.createNotification(title, content, NotificationType.INFORMATION)
        } else {
            Notification(NOTIFICATION_GROUP_ID, title, content, NotificationType.INFORMATION)
        }
        Notifications.Bus.notify(notification, project)
    }
    
    /**
     * 显示警告通知
     * 使用平台默认的自动消失时间（约 7 秒）
     */
    fun showWarning(project: Project?, title: String, content: String) {
        val group = getNotificationGroup()
        val notification = if (group != null) {
            group.createNotification(title, content, NotificationType.WARNING)
        } else {
            Notification(NOTIFICATION_GROUP_ID, title, content, NotificationType.WARNING)
        }
        Notifications.Bus.notify(notification, project)
    }
    
    /**
     * 显示错误通知
     * 错误通知通常不自动消失，需要用户手动关闭
     */
    fun showError(project: Project?, title: String, content: String) {
        val group = getNotificationGroup()
        val notification = if (group != null) {
            group.createNotification(title, content, NotificationType.ERROR)
        } else {
            Notification(NOTIFICATION_GROUP_ID, title, content, NotificationType.ERROR)
        }
        Notifications.Bus.notify(notification, project)
    }
}
