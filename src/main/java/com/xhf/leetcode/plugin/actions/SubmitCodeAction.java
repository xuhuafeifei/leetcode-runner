package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.RunCode;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmitCodeAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        // get cache
        String path = editor.getFile().getPath();
        path = FileUtils.unifyPath(path);
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
        assert lc != null;
        // build run code
        RunCode runCode = new RunCode();
        runCode.setQuestionId(lc.getQuestionId());
        runCode.setLang(lc.getLang());
        runCode.setTypeCode(codeContent);
        runCode.setTitleSlug(lc.getTitleSlug());

        CodeService.submitCode(project, runCode);
    }
}
