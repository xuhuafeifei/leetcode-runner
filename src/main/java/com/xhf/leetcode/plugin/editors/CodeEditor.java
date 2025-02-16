package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.Question;
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
 * 显示submission里的history code
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeEditor extends CopyToolBarEditor {
    /**
     * 填充code内容的容器
     */
    private final JTextPane textPane;
    /**
     * 核心容器
     */
    private final BorderLayoutPanel myComponent;


    @Override
    protected AnAction copyAction() {
        return new AnAction("复制代码", "复制代码", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // copy to clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(textPane.getText());
                clipboard.setContents(stringSelection, null);

                JOptionPane.showMessageDialog(null, "复制成功!");
            }
        };
    }

    @Override
    protected AnAction copyToAction() {
        return new AnAction("切换代码", "切换代码", AllIcons.Actions.Back) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String content = textPane.getText();
                Project project = e.getProject();
                if (project == null) {
                    JOptionPane.showMessageDialog(null, "Copy error because project is null");
                    return;
                }
                ConsoleUtils instance = ConsoleUtils.getInstance(project);
                String curContent = ViewUtils.getContentOfCurrentOpenVFile(project);
                boolean flag = ViewUtils.writeContentToCurrentVFile(project, Question.replaceCodeSnippets(curContent, content));
                if (flag) {
                    instance.showInfoWithoutConsole("切换成功!", false, true);
                }else {
                    instance.showWaringWithoutConsole("切换失败!", false, true);
                }
            }
        };
    }

    public CodeEditor(@NotNull Project project, @NotNull String code) {
        this.textPane = new JTextPane();
        this.textPane.setText(code); // set history code to text pane
        this.textPane.setFont(Constants.ENGLISH_FONT);
        this.textPane.setForeground(Constants.FOREGROUND_COLOR);
        this.textPane.setBackground(Constants.BACKGROUND_COLOR);
        this.textPane.setEditable(false);
        this.myComponent = JBUI.Panels.simplePanel();
        SplitEditorToolbar toolbarWrapper = createToolbarWrapper(this.textPane);
        this.myComponent.addToTop(toolbarWrapper);
        this.myComponent.addToCenter(this.textPane);
    }

    @Override
    public @NotNull JComponent getComponent() {
        // return jcefHtmlPanel.getComponent();
        return this.myComponent;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.myComponent;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Code Editor";
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
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
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
