package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.service.LoginService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractSplitTextEditor implements FileEditor {
    protected Project project;
    protected JComponent myComponent;
    protected VirtualFile file;
    protected JBSplitter jbSplitter;

    public AbstractSplitTextEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        initComponent();
    }

    private void initComponent() {
        jbSplitter = new JBSplitter();
        // set first
        initFirstComp();
        myComponent = JBUI.Panels.simplePanel(jbSplitter);
    }

    /**
     * when the second editor of splitter is not visible, the toolbar will be displayed in the second editor which contains close action
     * <p>
     * if the close action is activated, the second editor will be closed
     * @return
     */
    protected final SplitEditorToolbar createToolbarWrapper(JComponent comp) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(new AnAction("Close Solution", "Close solution", AllIcons.Actions.Close) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                closeSplitEditor();
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Solution", actionGroup, true);
        actionToolbar.setTargetComponent(comp);
        return new SplitEditorToolbar(null, actionToolbar);
    }

    private void closeSplitEditor() {
        jbSplitter.getSecondComponent().setVisible(false);
    }

    protected abstract void initFirstComp();

    @Override
    public final @NotNull JComponent getComponent() {
        return myComponent;
    }

    protected JTextPane showNotingTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 15));
        textPane.setForeground(new Color(110, 107, 107));
        textPane.setBackground(new Color(253, 255, 255));
        textPane.setEditable(false);
        return textPane;
    }

    /**
     * support open content ability
     */
    public abstract void openSecond(String content);

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
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
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    @Override
    public void dispose() {

    }
}
