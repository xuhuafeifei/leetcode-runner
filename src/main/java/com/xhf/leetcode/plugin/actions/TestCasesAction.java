package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestCasesAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e) {
        CodeService.openTestCasesDialog(project);
    }
}
