package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.DeepCodingEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * 用于切换正常刷题模式和deep coding刷题模式
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DeepCodingAction extends AbstractAction {

    public DeepCodingAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.DeepCoding"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        LCEventBus.getInstance().post(new DeepCodingEvent());
    }

}