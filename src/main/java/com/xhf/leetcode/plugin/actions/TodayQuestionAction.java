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

    /**
     * 每一次点击, 都会刷新每日一题解决状态
     * 之所以需要刷新, 是为了解决插件跨夜不关, 导致前一天的每日一题解决状态延续至今天
     */
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        QuestionService instance = QuestionService.getInstance(project);
        instance.updateTodayStatus();
        instance.todayQuestion(project);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    /**
     * 该方法会被多次且频繁调用, 因此尽量减少操作逻辑
     */
    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        // 根据某些条件动态设置图标
        QuestionService instance = QuestionService.getInstance(e.getProject());
        if (instance == null) {
            return;
        }
        if (instance.todayQuestionSolved() == 1) {
            presentation.setIcon(IconLoader.getIcon("/icons/flame.svg", this.getClass()));
            instance.modified();
        } else if (instance.todayQuestionSolved() == -1){
            presentation.setIcon(IconLoader.getIcon("/icons/daily.svg", this.getClass()));
            instance.modified();
        }
    }
}
