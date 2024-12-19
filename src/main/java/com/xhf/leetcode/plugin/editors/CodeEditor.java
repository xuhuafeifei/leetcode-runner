package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import jnr.constants.Constant;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;
import java.util.Map;

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
        return new AnAction("Copy", "Copy", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // copy to clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(textPane.getText());
                clipboard.setContents(stringSelection, null);

                JOptionPane.showMessageDialog(null, "Copy Success");
            }
        };
    }

    @Override
    protected AnAction copyToAction() {
        return new AnAction("Copy to Content Editor", "Copy to content editor", AllIcons.Actions.Back) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String content = textPane.getText();
                Project project = e.getProject();
                if (project == null) {
                    JOptionPane.showMessageDialog(null, "Copy error because project is null");
                    return;
                }
                ConsoleUtils instance = ConsoleUtils.getInstance(project);
                boolean flag = ViewUtils.writeContentToCurrentVFile(project, Question.handleCodeSnippets(content, AppSettings.getInstance().getLangType()));
                if (flag) {
                    instance.showInfo("Copy To Editor Success", false, true);
                }else {
                    instance.showWaring("Copy To Editor Error", false, true);
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
