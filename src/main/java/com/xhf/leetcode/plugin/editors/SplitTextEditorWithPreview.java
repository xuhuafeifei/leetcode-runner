package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import org.jetbrains.annotations.NotNull;

public class SplitTextEditorWithPreview extends TextEditorWithPreview {

    public SplitTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview) {
//        super(editor, preview);
        super(editor, preview, "Question " + Layout.SHOW_EDITOR_AND_PREVIEW.getName(), Layout.SHOW_EDITOR_AND_PREVIEW);
    }
}
