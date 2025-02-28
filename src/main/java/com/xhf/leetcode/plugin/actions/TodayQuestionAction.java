package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.xhf.leetcode.plugin.service.QuestionService;
import org.jetbrains.annotations.NotNull;

/**
 * get daily question
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TodayQuestionAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        QuestionService.getInstance(project).todayQuestion(project);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        // 根据某些条件动态设置图标
        QuestionService instance = QuestionService.getInstance(e.getProject());
        if (instance.todayQuestionSolved() == 1) {
            presentation.setIcon(IconLoader.getIcon("/icons/flame.svg", this.getClass()));
            instance.modified();
        } else if (instance.todayQuestionSolved() == -1){
            presentation.setIcon(IconLoader.getIcon("/icons/daily.svg", this.getClass()));
            instance.modified();
        }
    }
}
