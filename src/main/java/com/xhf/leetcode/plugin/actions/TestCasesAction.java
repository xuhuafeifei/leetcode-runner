package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestCasesAction extends AbstractAction {

    public TestCasesAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.editor.TestCasesAction"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        CodeService.getInstance(project).openTestCasesDialog();
    }

}