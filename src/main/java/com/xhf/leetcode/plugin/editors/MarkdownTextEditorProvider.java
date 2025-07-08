package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.Article;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.MarkdownContentType;
import com.xhf.leetcode.plugin.utils.OSHandler;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import java.util.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MarkdownTextEditorProvider implements AsyncFileEditorProvider, DumbAware {

    public MarkdownTextEditorProvider() {

    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        filePath = OSHandler.unifyFileSeparator(filePath, "/");
        String[] split = filePath.split("/");
        String fileName = split[split.length - 1];
        return fileName.startsWith("[0x3f]");
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
                HashMap<String, Object> map = new HashMap<>();
                String articleUrl = ViewUtils.getContentFromVFile(file);
                Article article = LeetcodeClient.getInstance(project).queryArticle(articleUrl);
                map.put(Constants.ARTICLE_URL, articleUrl);
                map.put(Constants.VFILE, file);
                map.put(Constants.ARTICLE_CONTENT, article.getContent());
                map.put(Constants.ARTICLE_TITLE, article.getTitle());
                return new MarkDownEditor(project, map, MarkdownContentType._0x3f);
            }
        };
    }
}
