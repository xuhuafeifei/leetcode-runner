package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import org.jetbrains.annotations.NotNull;

public class PickOneAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        // choose one question randomly
        Question question = QuestionService.getInstance().pickOne(project);
        CodeService.openCodeEditor(question, project);
    }
}
