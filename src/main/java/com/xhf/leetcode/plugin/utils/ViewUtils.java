package com.xhf.leetcode.plugin.utils;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ViewUtils {
    public static <T> T getFileEditor(Project project, Class<T> tClass) {
        /* get file editor */
        FileEditorManager manager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = manager.getSelectedEditor();
        // assert, tClass must be SplitTextEditorWithPreview because other's editor not be recited
        assert tClass == SplitTextEditorWithPreview.class;
        if (!tClass.isInstance(selectedEditor)) {
            throw new IllegalArgumentException("Selected editor is not an instance of " + tClass.getName());
        }
        T editor = (T) selectedEditor;
        return editor;
    }

    public static LeetcodeEditor getLeetcodeEditorByVFile(VirtualFile file, Project project) {
        String key = file.getPath();
        key = FileUtils.unifyPath(key);
        return StoreService.getInstance(project).getCache(key, LeetcodeEditor.class);
    }

    public static LightVirtualFile getHTMLContent(VirtualFile file, Project project) {
        // get preview file
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);

        // need to refresh a current file system. otherwise, it will take a lot of time to find the file
        // VirtualFile previewFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(contentPath);
        file.refresh(true, false);
        assert lc != null;
        return new LightVirtualFile(lc.getTitleSlug() + ".html", lc.getMarkdownContent());
    }

}
