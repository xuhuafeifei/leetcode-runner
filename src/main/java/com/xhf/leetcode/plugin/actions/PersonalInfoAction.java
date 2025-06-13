package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;

/**
 * @author xuhuafei
 */
public class PersonalInfoAction extends AbstractAction {

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        ActionUtils.createPersonalWindow(project);
    }
}
