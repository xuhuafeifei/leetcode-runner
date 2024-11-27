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
public class RunCodeAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        assert lc != null;
        RunCode runCode = super.buildRunCode(lc, codeContent);
        CodeService.runCode(project, runCode);
    }
}
