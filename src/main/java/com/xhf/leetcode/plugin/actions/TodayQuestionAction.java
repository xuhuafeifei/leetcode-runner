package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xhf.leetcode.plugin.service.QuestionService;
import org.jetbrains.annotations.NotNull;

/**
 * get daily question
 */
public class TodayQuestionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        QuestionService.getInstance().todayQuestion(e.getProject());
    }
}
