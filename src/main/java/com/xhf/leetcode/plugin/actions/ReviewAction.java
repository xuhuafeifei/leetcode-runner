package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ReviewAction extends AbstractAction {

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        ActionUtils.createReviewWindow(project);
    }
}
