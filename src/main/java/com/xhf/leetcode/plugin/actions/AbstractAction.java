package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LoginPass;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        // settings check
        AppSettings appSettings = AppSettings.getInstance();
        if (! appSettings.initOrNot()) {
            ConsoleUtils.getInstance(project).showWaring("please init plugin setting");
            return;
        }
        // login check
        LoginPass annotation = this.getClass().getAnnotation(LoginPass.class);
        if (annotation == null) {
            boolean login = LoginService.isLogin(project);
            if (! login) {
                ConsoleUtils.getInstance(e.getProject()).showWaring("not login!");
                return;
            }
        }
        doActionPerformed(project, e);
    }

    abstract void doActionPerformed(Project project, AnActionEvent e);
}
