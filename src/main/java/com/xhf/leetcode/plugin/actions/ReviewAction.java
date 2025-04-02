package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ReviewAction extends AbstractAction {
    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        if (1 == 1) {
            ConsoleUtils.getInstance(project).showInfo(BundleUtils.i18nHelper("正在开发中, 敬请期待", "under development, please wait"), true, true);
            return ;
        }
        ActionUtils.createReviewWindow(project);
    }
}
