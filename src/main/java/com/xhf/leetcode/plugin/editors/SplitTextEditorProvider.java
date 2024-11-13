package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SplitTextEditorProvider implements AsyncFileEditorProvider, DumbAware {
    private final TextEditorProvider first;

    private final FileEditorProvider second;

    public SplitTextEditorProvider() {
        first = new PsiAwareTextEditorProvider();
        second = new MarkDownEditorProvider();
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        // check if a file is created by CodeService, each code file created by this module will be cached in StoreService
        String key = file.getPath(); // file separator is /
        key = FileUtils.unifyPath(key);
        return StoreService.getInstance(project).contains(key);
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return this.createEditorAsync(project, file).build();
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "demo";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        // HIDE_DEFAULT_EDITOR is supported only for DumbAware providers
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @Override
    public @NotNull Builder createEditorAsync(@NotNull Project project, @NotNull VirtualFile file) {

        // get preview file
        String key = file.getPath();
        key = FileUtils.unifyPath(key);
        String contentPath = StoreService.getInstance(project).getCache(key, String.class);

        assert contentPath != null;
        // need to refresh a current file system. otherwise, it will take a lot of time to find the file
        VirtualFile previewFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(contentPath);
        return new Builder() {
            @Override
            public FileEditor build() {
                return new SplitTextEditorWithPreview((TextEditor) first.createEditor(project, file),
                        second.createEditor(project, previewFile));
            }
        };
    }
}
