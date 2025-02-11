package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.QuestionService;

/**
 * get daily question
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TodayQuestionAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        QuestionService.getInstance().todayQuestion(project);
    }
}
