package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SplitTextEditorProvider implements AsyncFileEditorProvider, DumbAware {
    private final TextEditorProvider first;

    private final FileEditorProvider second;

    public SplitTextEditorProvider() {
        first = new PsiAwareTextEditorProvider();
        // second = new MarkDownEditorProvider();
        second = new FocusTextEditorProvider();
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        // check if a file is created by CodeService, each code file created by this module will be cached in StoreService
        String key = ViewUtils.getUnifyFilePathByVFile(file);
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
        return new Builder() {
            @Override
            public FileEditor build() {
                return new SplitTextEditorWithPreview((TextEditor) first.createEditor(project, file),
                        second.createEditor(project, file));
            }
        };
    }
}
