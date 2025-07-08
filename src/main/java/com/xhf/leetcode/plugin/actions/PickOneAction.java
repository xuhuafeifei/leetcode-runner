package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

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
        try {
            CodeService.getInstance(project).openCodeEditor(question);
        } catch (FileCreateError ex) {
            LogUtils.error(ex);
            ConsoleUtils.getInstance(project)
                .showError(BundleUtils.i18n("code.service.file.create.error") + "\n" + ex.getMessage(), true, true);
        }
    }

}
