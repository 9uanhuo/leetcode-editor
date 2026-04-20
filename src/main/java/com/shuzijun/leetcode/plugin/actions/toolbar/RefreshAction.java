package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

/**
 * @author shuzijun
 */
public class RefreshAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        NavigatorAction navigatorAction = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        navigatorAction.getFind().operationType("");
        navigatorAction.findClear();
    }


}
