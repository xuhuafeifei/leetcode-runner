package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class FocusTextEditor implements FileEditor {
    private Project project;
    private JComponent myComponent;
    private VirtualFile file;
    private JBTabbedPane tabbedPane;

    public FocusTextEditor(Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;
        initComponent();
    }

    private void initComponent() {
        tabbedPane = new JBTabbedPane();

        JComponent contentPanel = new MarkDownEditor(project, ViewUtils.getHTMLContent(file, project)).getComponent();
        JComponent solutionPanel = new SolutionEditor(project, file).getComponent();
        JComponent submissionPanel = new SubmissionEditor(project, file).getComponent();

        tabbedPane.addTab("content", contentPanel);
        tabbedPane.addTab("solution", solutionPanel);
        tabbedPane.addTab("submission", submissionPanel);

        myComponent = tabbedPane;
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
