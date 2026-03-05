package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class FocusTextEditorProvider implements AsyncFileEditorProvider, DumbAware {

    @Override
    public @NotNull Builder createEditorAsync(@NotNull Project project, @NotNull VirtualFile file) {
        return new Builder() {
            @Override
            public FileEditor build() {
                return new FocusTextEditor(project, file);
            }
        };
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return true;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return this.createEditorAsync(project, file).build();
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "Focus Text Editor Provider";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
