package com.shuzijun.leetcode.plugin.listener

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.ui.jcef.JBCefApp
import com.shuzijun.leetcode.plugin.model.PluginConstant

class SupportCheck : ProjectActivity, DumbAware {

    override suspend fun execute(project: Project) {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }
        if (!JBCefApp.isSupported()) {
            Notifications.Bus.notify(
                Notification(
                    PluginConstant.NOTIFICATION_GROUP,
                    "Not Support JCEF",
                    "Your environment does not support JCEF, cannot use LeetCode Editor.Check the Registry 'ide.browser.jcef.enabled'.",
                    NotificationType.ERROR
                )
            )
        }
    }
}