package com.xhf.leetcode.plugin.editors.text;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

import static com.intellij.openapi.actionSystem.ActionPlaces.TEXT_EDITOR_WITH_PREVIEW;

public class CustomTextEditor implements TextEditor {
    private final Editor editor;
    private final JPanel component;
    private final VirtualFile file;

    public CustomTextEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            throw new IllegalStateException("Cannot create editor for file: " + file.getName());
        }

        editor = EditorFactory.getInstance().createEditor(document, project, file.getFileType(), false);

        EditorHighlighter highlighter = EditorHighlighterFactory.getInstance()
                .createEditorHighlighter(project, file);
        ((EditorEx) editor).setHighlighter(highlighter);

        component = new JPanel(new BorderLayout());
        component.add(createToolbar(), BorderLayout.NORTH);
        component.add(editor.getComponent(), BorderLayout.CENTER);
    }

    private JComponent createToolbar() {
        ActionGroup action = (ActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.text.group");
        DefaultActionGroup dag = new DefaultActionGroup();
        // 增加语言图标
        dag.add(ViewUtils.createLangIcon());
        dag.addSeparator();
        dag.add(action);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("CustomTextEditor", dag, true);
        toolbar.setTargetComponent(editor.getComponent());
        return toolbar.getComponent();
    }

    // TextEditor 接口特有
    @Override
    public @NotNull Editor getEditor() {
        return editor;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return component;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }

    @Override
    public @NotNull String getName() {
        return "Custom Text Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }

    @Override public void selectNotify() {}
    @Override public void deselectNotify() {}

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override public boolean isModified() { return false; }
    @Override public boolean isValid() { return true; }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {

    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    @Override
    public VirtualFile getFile() {
        return file;
    }
}