package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * 恢复默认代码
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DefaultContentAction extends AbstractAction {

    public DefaultContentAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.DefaultContent"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        CodeService.getInstance(project).getDefaultContent();
    }

}
