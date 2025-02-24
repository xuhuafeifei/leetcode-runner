package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.OutputDetail;
import com.xhf.leetcode.plugin.model.SubmissionDetail;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;

/**
 * 错误信息editor, 用于显示submission的error info
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ErrorInfoEditor extends CopyToolBarEditor {
    private final @NotNull BorderLayoutPanel myComponent;
    private final SubmissionDetail sd;

    public ErrorInfoEditor(Project project, SubmissionDetail sd) {
        this.sd = sd;
        OutputDetail od = sd.getOutputDetail();
        String status = sd.getStatusDisplay();

        FormBuilder fb = FormBuilder.createFormBuilder();
        fb.addLabeledComponent("状态", createContentLabel(status));
        fb.addSeparator();

        switch (status) {
            case "Wrong Answer":
            case "Memory Limit Exceeded":
            case "Runtime Error":
            case "Time Limit Exceeded":
                fb.addLabeledComponent("上一个测试案例:", createContentLabel(od.getLastTestcase()));
                fb.addSeparator();
                fb.addLabeledComponent("期待结果:", createContentLabel(od.getExpectedOutput()));
                fb.addSeparator();
                fb.addLabeledComponent("你的结果:", createContentLabel(od.getCodeOutput()));
                fb.addSeparator();
                fb.addLabeledComponent("标准输出:", createContentLabel(sd.getStdOutput()));
                break;
            case "Compile Error":
                fb.addLabeledComponent("编译错误:", createContentLabel(od.getCompileError()));
                break;
        }
        fb.addSeparator();
        JPanel panel = fb.getPanel();

        // 为核心(panel)内容创建toolbar
        this.myComponent = JBUI.Panels.simplePanel();
        SplitEditorToolbar toolbarWrapper = createToolbarWrapper(panel);
        this.myComponent.addToTop(toolbarWrapper);
        this.myComponent.addToCenter(panel);
    }

    private JTextPane createContentLabel(String text) {
        JTextPane textPane = new JTextPane();
        textPane.setText(text);
        textPane.setFont(Constants.CN_FONT);
        textPane.setForeground(Constants.FOREGROUND_COLOR);
        textPane.setBackground(Constants.BACKGROUND_COLOR);
        textPane.setEditable(false);
        return textPane;
    }

    /**
     * 复制测试用例
     * @return
     */
    @Override
    protected final AnAction copyAction() {
        return new AnAction("复制测试案例", "复制测试案例", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // copy to clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(sd.getOutputDetail().getLastTestcase());
                clipboard.setContents(stringSelection, null);

                JOptionPane.showMessageDialog(null, "复制测试案例成功!");
            }
        };
    }

    @Override
    protected final AnAction copyToAction() {
        return new AnAction("新增测试案例", "新增测试案例成功!", IconLoader.getIcon("/icons/switch.svg", ErrorInfoEditor.class)) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Project project = e.getProject();
                if (project == null) {
                    JOptionPane.showMessageDialog(null, "Copy error because project is null");
                    return;
                }
                // 获取lc
                LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByCurrentVFile(project);
                if (lc == null) {
                    JOptionPane.showMessageDialog(null, "新增测试案例错误! 请关闭文件或重定位题目");
                    return;
                }
                String exampleTestcases = lc.getExampleTestcases();
                // 获取last测试案例
                String content = sd.getOutputDetail().getLastTestcase();
                if (! endsWithNewline(exampleTestcases)) {
                    exampleTestcases = exampleTestcases + "\n" + content;
                }
                lc.setExampleTestcases(exampleTestcases);
                // 更新cache
                boolean flag = ViewUtils.updateLeetcodeEditorByCurrentVFile(project, lc);
                if (flag) {
                    ConsoleUtils.getInstance(project).showInfoWithoutConsole("新增测试案例成功!", false, true);
                } else {
                    ConsoleUtils.getInstance(project).showInfoWithoutConsole("新增测试案例失败!", false, true);
                }
            }
        };
    }

    /**
     * 检测是不是以换行结尾
     * @param str
     * @return
     */
    public boolean endsWithNewline(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int lastCharIndex = str.length() - 1;
        char lastChar = str.charAt(lastCharIndex);
        if (lastChar == '\n') {
            return true;
        } else if (lastChar == '\r' && str.length() > 1 && str.charAt(lastCharIndex - 1) == '\n') {
            return true;
        }
        return false;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return myComponent;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return null;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }
}
