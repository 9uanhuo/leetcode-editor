package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project-level service that manages the cached LeetCode user per URL.
 *
 * @author shuzijun
 */
public class UserContext {

    private final Map<String, User> userCache = new ConcurrentHashMap<>();

    public static UserContext getInstance(Project project) {
        return project.getService(UserContext.class);
    }

    @NotNull
    public User getUser() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) return new User();
        return userCache.computeIfAbsent(config.getUrl(), k -> QuestionManager.getUser());
    }

    public void setUser(String host, User user) {
        userCache.put(host, user);
    }
}
