package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * 移除选定提交代码区域注释
 */
public class RemoveCommentAction extends AbstractAction {

    public RemoveCommentAction() {
        super(BundleUtils.i18n("action.leetcode.plugin.RemoveCommentAction"));
    }

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        // 获取当前代码内容
        String content = ViewUtils.getContentOfCurrentOpenVFile(project);
        content = Question.removeComment(content);
        ViewUtils.writeContentToCurrentVFile(project, content);
        // ConsoleUtils.getInstance(project).showInfo(BundleUtils.i18n("action.leetcode.actions.command.remove"), false, true);
    }

}
