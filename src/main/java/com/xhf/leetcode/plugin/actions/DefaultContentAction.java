package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.CodeService;

/**
 * 恢复默认代码
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DefaultContentAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        CodeService.getInstance(project).getDefaultContent();
    }
}
