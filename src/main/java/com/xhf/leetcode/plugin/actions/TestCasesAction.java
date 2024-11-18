package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.QuestionService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TestCasesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        /* get file editor */
        Project project = e.getProject();
        FileEditorManager manager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = manager.getSelectedEditor();
        assert selectedEditor instanceof SplitTextEditorWithPreview;
        SplitTextEditorWithPreview editor = (SplitTextEditorWithPreview) selectedEditor;

        // get example test cases
        String path = editor.getFile().getPath();
        path = FileUtils.unifyPath(path);
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);

        // check data input
        assert lc != null;
        if (lc.getExampleTestcases() == null || lc.getDefaultTestcases() == null) {
            // load example
            Question q = QuestionService.getInstance().queryQuestionInfo(lc.getTitleSlug(), project);
            if (StringUtils.isBlank(q.getExampleTestcases())) {
                throw new RuntimeException("No example test cases found...");
            }
            lc.setExampleTestcases(q.getExampleTestcases());
            lc.setDefaultTestcases(q.getExampleTestcases());
            // restore
            StoreService.getInstance(project).addCache(path, lc);
        }

        // create dialog
        new TestCasesDialog(
                lc.getExampleTestcases(), path, project
        ).show();
    }

    private static class TestCasesDialog extends DialogWrapper {
        private String dataInput;
        private JPanel contentPane;
        private JTextArea textArea;
        // java file path, which is used for a key in cache
        private String path;
        private Project project;
        private JButton resetButton;

        public TestCasesDialog(String dataInput, String path, Project project) {
            super(true);
            this.dataInput = dataInput;
            this.path = path;
            this.project = project;
            init();
            setTitle("Set Test Cases");
            setSize(600, 400);
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            contentPane = new JPanel(new BorderLayout());
            textArea = new JTextArea(400, 400);
            textArea.setText(this.dataInput);
            textArea.setEditable(true);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPane.add(scrollPane, BorderLayout.CENTER);

            return contentPane;
        }

        @Override
        protected @Nullable JComponent createSouthPanel() {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            // create reset button
            resetButton = new JButton("Reset");
            resetButton.setBorderPainted(false);
            resetButton.setContentAreaFilled(false);
            resetButton.setFocusPainted(false);
            resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            resetButton.addActionListener(e -> {
                // get default data
                LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
                String defaultTestcases = lc.getDefaultTestcases();
                textArea.setText(defaultTestcases);
            });
            buttonPanel.add(resetButton);


            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> doOKAction());
            buttonPanel.add(okButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> doCancelAction());
            buttonPanel.add(cancelButton);

            return buttonPanel;
        }

        @Override
        protected void doOKAction() {
            String inputText = textArea.getText();
            inputText = inputText.trim();
            // update data
            LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
            lc.setExampleTestcases(inputText);
            StoreService.getInstance(project).addCache(path, lc);
            super.doOKAction();
        }
    }
}
