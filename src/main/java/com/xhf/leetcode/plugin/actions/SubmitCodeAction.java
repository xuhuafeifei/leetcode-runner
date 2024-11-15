package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.RunCode;
import com.xhf.leetcode.plugin.service.CodeService;
import org.jetbrains.annotations.NotNull;

public class SubmitCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        /* get file editor */
        Project project = e.getProject();
        FileEditorManager manager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = manager.getSelectedEditor();
        assert selectedEditor instanceof SplitTextEditorWithPreview;
        SplitTextEditorWithPreview editor = (SplitTextEditorWithPreview) selectedEditor;

        // get file content
        String codeContent = editor.getFileContent();
        // get cache
        String path = editor.getFile().getPath();
        path = FileUtils.unifyPath(path);
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
        // build run code
        RunCode runCode = new RunCode();
        runCode.setQuestionId(lc.getQuestionId());
        runCode.setLang(lc.getLang());
        runCode.setTypeCode(codeContent);
        runCode.setTitleSlug(lc.getTitleSlug());

        CodeService.submitCode(project, runCode);
    }
}
