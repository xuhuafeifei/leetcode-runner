package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

public class AddCommentAction extends AbstractAction{
    public AddCommentAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.AddCommentAction"));
    }

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        // 获取当前代码内容
        String content = ViewUtils.getContentOfCurrentOpenVFile(project);
        content = Question.addComment(content);
        ViewUtils.writeContentToCurrentVFile(project, content);
        // ConsoleUtils.getInstance(project).showInfo(BundleUtils.i18n("action.leetcode.actions.comment.info"), false, true);
    }
}
