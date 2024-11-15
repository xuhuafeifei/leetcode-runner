package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SplitTextEditorWithPreview extends TextEditorWithPreview {

    public SplitTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview) {
        super(editor, preview, "Question " + Layout.SHOW_EDITOR_AND_PREVIEW.getName(), Layout.SHOW_EDITOR_AND_PREVIEW);
    }

    public String getFileContent() {
        return this.getEditor().getDocument().getText();
    }


    @Nullable
    protected ActionGroup createLeftToolbarActionGroup() {
        return (ActionGroup) ActionManager.getInstance().getAction("leetcode.editor.group");
    }
}
