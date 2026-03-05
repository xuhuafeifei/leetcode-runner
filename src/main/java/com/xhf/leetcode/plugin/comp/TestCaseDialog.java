package com.xhf.leetcode.plugin.comp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestCaseDialog extends DialogWrapper {

    private final String dataInput;
    // java file path, which is used for a key in cache
    private final String path;
    private final Project project;
    private JTextArea textArea;

    public TestCaseDialog(String dataInput, String path, Project project) {
        super(true);
        this.dataInput = dataInput;
        this.path = path;
        this.project = project;
        init();
        setTitle(BundleUtils.i18n("comp.leetcode.testcase"));
        setSize(600, 400);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel contentPane = new JPanel(new BorderLayout());
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
        JButton resetButton = new JButton(BundleUtils.i18n("comp.leetcode.reset"));
        resetButton.setBorderPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> {
            // get default data
            LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
            if (lc == null) {
                ViewUtils.getDialogWrapper(BundleUtils.i18n("comp.leetcode.testcase.fetch.failed"));
                return;
            }
            String defaultTestcases = lc.getDefaultTestcases();
            textArea.setText(defaultTestcases);
        });
        buttonPanel.add(resetButton);

        JButton okButton = new JButton(BundleUtils.i18n("action.leetcode.plugin.ok"));
        okButton.addActionListener(e -> doOKAction());
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton(BundleUtils.i18n("action.leetcode.plugin.cancel"));
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
        if (lc == null) {
            ViewUtils.getDialogWrapper(BundleUtils.i18n("comp.leetcode.testcase.set.failed"));
            return;
        }
        lc.setExampleTestcases(inputText);
        StoreService.getInstance(project).addCache(path, lc);
        super.doOKAction();
    }
}
