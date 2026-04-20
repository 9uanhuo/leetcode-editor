package com.shuzijun.leetcode.plugin.listener

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.sentry.Sentry


/**
 * @author jionghui.zheng
 * @since 2026/4/21 01:19
 */
class SentryStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!Sentry.isEnabled()) {
            Sentry.init { options ->
                options.dsn = "https://ac9e2d69c3294870848cee5b1b23ad51@sentry.io/1534194"
            }
        }
    }
}