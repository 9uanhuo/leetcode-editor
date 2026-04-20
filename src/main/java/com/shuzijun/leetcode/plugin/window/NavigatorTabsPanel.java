package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.User;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.setting.UserContext;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.navigator.AllNavigatorPanel;
import com.shuzijun.leetcode.plugin.window.navigator.NavigatorPanel;
import com.shuzijun.leetcode.plugin.window.navigator.TopNavigatorPanel;

import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class NavigatorTabsPanel extends SimpleToolWindowPanel implements Disposable {

    private final Project project;

    private SimpleToolWindowPanel[] navigatorPanels;
    private TabInfo[] tabInfos;

    private JBTabsImpl tabs;

    private int toggleIndex = 0;

    public NavigatorTabsPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        navigatorPanels = new SimpleToolWindowPanel[3];
        tabInfos = new TabInfo[3];

        tabs = new JBTabsImpl(project);
        tabs.setHideTabs(true);

        NavigatorPanel navigatorPanel = new NavigatorPanel(toolWindow, project);
        navigatorPanels[0] = navigatorPanel;

        TabInfo tabInfo = new TabInfo(navigatorPanel);
        tabInfo.setText("page");
        tabInfos[0] = tabInfo;
        tabs.addTab(tabInfo);

        AllNavigatorPanel allNavigatorPanel = new AllNavigatorPanel(toolWindow, project);
        navigatorPanels[1] = allNavigatorPanel;

        TabInfo allTabInfo = new TabInfo(allNavigatorPanel);
        allTabInfo.setText("all");
        tabInfos[1] = allTabInfo;
        tabs.addTab(allTabInfo);

        TopNavigatorPanel topNavigatorPanel = new TopNavigatorPanel(toolWindow, project);
        navigatorPanels[2] = topNavigatorPanel;

        TabInfo topTabInfo = new TabInfo(topNavigatorPanel);
        topTabInfo.setText("codeTop");
        tabInfos[2] = topTabInfo;
        tabs.addTab(topTabInfo);

        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            for (int i = 0; i < tabInfos.length; i++) {
                if (tabInfos[i].getText().equalsIgnoreCase(config.getNavigatorName())) {
                    tabs.select(tabInfos[i], true);
                    toggleIndex = i;
                    break;
                }
            }
        }

        setContent(tabs);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            User user = getUser();
            if (user.isSignedIn()) {
                WindowFactory.updateTitle(project, user.getUsername());
                StatisticsData.refresh(project);
            } else {
                WindowFactory.updateTitle(project, "No login");
            }
        });

        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        messageBusConnection.subscribe(LoginNotifier.TOPIC, new LoginNotifier() {
            @Override
            public void login(Project notifierProject, String host) {
                User user = getUser();
                if (user.isSignedIn()) {
                    WindowFactory.updateTitle(project, user.getUsername());
                    StatisticsData.refresh(project);
                } else {
                    WindowFactory.updateTitle(project, "No login");
                }
            }

            @Override
            public void logout(Project notifierProject, String host) {
                WindowFactory.updateTitle(project, "No login");
            }
        });
        messageBusConnection.subscribe(ConfigNotifier.TOPIC, new ConfigNotifier() {
            @Override
            public void change(Config oldConfig, Config newConfig) {
                if (oldConfig != null && !oldConfig.getUrl().equalsIgnoreCase(newConfig.getUrl())) {
                    User user = getUser();
                    if (user.isSignedIn()) {
                        WindowFactory.updateTitle(project, user.getUsername());
                        StatisticsData.refresh(project);
                    } else {
                        WindowFactory.updateTitle(project, "No login");
                    }
                }
            }
        });
        messageBusConnection.subscribe(QuestionStatusNotifier.QUESTION_STATUS_TOPIC, (QuestionStatusNotifier) question -> StatisticsData.refresh(project));

        for (SimpleToolWindowPanel n : navigatorPanels) {
            if (n != null && navigatorPanel instanceof Disposable) {
                Disposer.register(this, (Disposable) n);
            }
        }
    }

    public void toggle() {
        toggleIndex = (toggleIndex + 1) % 3;
        tabs.select(tabInfos[toggleIndex], true);
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            config.setNavigatorName(tabInfos[toggleIndex].getText());
            PersistentConfig.getInstance().setInitConfig(config);
        }
    }

    @NotNull
    public User getUser() {
        return UserContext.getInstance(project).getUser();
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink sink) {
        for (SimpleToolWindowPanel navigatorPanel : navigatorPanels) {
            navigatorPanel.uiDataSnapshot(sink);
        }
        sink.set(DataKeys.LEETCODE_PROJECTS_TABS, this);
        SimpleToolWindowPanel panel = navigatorPanels[toggleIndex];
        if (panel instanceof NavigatorPanelAction) {
            sink.set(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION, ((NavigatorPanelAction) panel).getNavigatorAction());
        }
    }

    @Override
    public void dispose() {
        for (SimpleToolWindowPanel navigatorPanel : navigatorPanels) {
            if (navigatorPanel != null && navigatorPanel instanceof Disposable) {
                ((Disposable) navigatorPanel).dispose();
            }
        }
    }

    public static synchronized void loadUser(boolean login) {
        User user;
        if (login) {
            user = null;
            for (int i = 0; i <= 50; i++) {
                user = QuestionManager.getUser();
                if (!user.isSignedIn()) {
                    try {
                        Thread.sleep(500 + (i / 10 * 100));
                    } catch (InterruptedException ignore) {
                    }
                } else {
                    break;
                }
                if (i == 50) {
                    LogUtils.LOG.warn("User data is not synchronized");
                }
            }
        } else {
            user = new User();
        }
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            UserContext.getInstance(project).setUser(URLUtils.getLeetcodeHost(), user);
        }
    }
}
