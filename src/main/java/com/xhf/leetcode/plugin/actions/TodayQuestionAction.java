package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * get daily question
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TodayQuestionAction extends AbstractAction {

    public TodayQuestionAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.TodayQuestionAction"));
    }

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

//    @Override
//    public @NotNull ActionUpdateThread getActionUpdateThread() {
//        return super.getActionUpdateThread();
//    }

    /**
     * 该方法会被多次且频繁调用, 因此尽量减少操作逻辑
     */
    @Override
    public void update(AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }
        // todo: 未来记得删了方法耗时记录功能, 该功能只是为了判断update这个高频调用的方法是否会导致项目加载速度变慢
        // var start = System.currentTimeMillis();
        // LogUtils.simpleDebug("start to update...");
        Presentation presentation = e.getPresentation();
        // 根据某些条件动态设置图标
        QuestionService instance = QuestionService.getInstance(e.getProject());
        if (instance.todayQuestionSolved() == 1) {
            presentation.setIcon(IconLoader.getIcon("/icons/flame.svg", this.getClass()));
            // 获取当日连击次数
            presentation.setText(BundleUtils.i18n("action.leetcode.plugin.TodayQuestionAction") + " " + instance.getTodayQuestionCount());
            instance.modified();
        } else if (instance.todayQuestionSolved() == -1){
            presentation.setIcon(IconLoader.getIcon("/icons/daily.svg", this.getClass()));
            presentation.setText(BundleUtils.i18n("action.leetcode.plugin.TodayQuestionAction"));
            instance.modified();
        }
        // LogUtils.simpleDebug("end update..., take = " + (System.currentTimeMillis() - start));
    }
}
