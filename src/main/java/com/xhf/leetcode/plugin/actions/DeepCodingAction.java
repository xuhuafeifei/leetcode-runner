package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.DeepCodingEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DeepCodingAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        LCEventBus.getInstance().post(new DeepCodingEvent());
    }
}
