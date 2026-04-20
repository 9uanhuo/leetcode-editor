package com.shuzijun.leetcode.plugin.utils;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;
import io.sentry.protocol.User;

/**
 * @author shuzijun
 */

public class SentryUtils {

    public static void submitErrorReport(Throwable error, String description) {

        Sentry.withScope(scope -> {

            // ===== OS 信息 =====
            scope.setContexts("os", Map.of(
                    "name", SystemInfo.OS_NAME,
                    "version", SystemInfo.OS_VERSION,
                    "kernel_version", SystemInfo.OS_ARCH
            ));

            // ===== Runtime 信息 =====
            ApplicationInfo applicationInfo = ApplicationInfo.getInstance();

            scope.setContexts("runtime", Map.of(
                    "name", applicationInfo.getBuild().getProductCode(),
                    "version", applicationInfo.getFullVersion()
            ));

            // ===== 描述 =====
            if (!StringUtil.isEmptyOrSpaces(description)) {
                scope.setTag("with-description", "true");
                scope.setExtra("description", description);
            }

            // ===== 用户信息 =====
            Config config = PersistentConfig.getInstance().getInitConfig();
            if (config != null) {
                User user = getUser(config);
                scope.setUser(user);
            }

            // ===== tags =====
            scope.setTag("javaVersion", SystemInfo.JAVA_RUNTIME_VERSION);

            String pluginVersion = PluginManagerCore
                    .getPlugin(PluginId.getId(PluginConstant.PLUGIN_ID))
                    .getVersion();

            scope.setTag("pluginVersion", pluginVersion);

            // ===== 发送 =====
            if (error == null) {
                Sentry.captureMessage(description);
            } else {
                Sentry.captureException(error);
            }
        });
    }

    private static @NotNull User getUser(Config config) {
        User user = new User();
        user.setId(config.getId());

        Map<String, String> data = new HashMap<>();
        data.put("version", String.valueOf(config.getVersion()));
        data.put("codeType", config.getCodeType());
        data.put("url", config.getUrl());
        data.put("proxy", String.valueOf(config.getProxy()));
        data.put("customCode", String.valueOf(config.getCustomCode()));
        data.put("customFileName", config.getCustomFileName());
        data.put("customTemplate", config.getCustomTemplate());

        user.setData(data);
        return user;
    }
}
