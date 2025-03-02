package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PickOneAction extends AbstractAction {

    public PickOneAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.PickOneAction"));
    }

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        // choose one question randomly
        Question question = QuestionService.getInstance(project).pickOne(project);
        CodeService.getInstance(project).openCodeEditor(question);
    }

}
