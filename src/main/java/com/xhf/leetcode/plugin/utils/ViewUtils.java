package com.xhf.leetcode.plugin.utils;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;

import javax.swing.*;

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
        String key = getCacheKeyByVFile(file);
        LeetcodeEditor cache = StoreService.getInstance(project).getCache(key, LeetcodeEditor.class);
        if (cache == null) {
            LogUtils.warn("LeetcodeEditor load failed! this may caused by two reason. 1.cache file is crashed. 2.key is wrong. Here " +
                    "are detail infomation : key="+key + " cache_file_path=" + StoreService.getInstance(project).getCacheFilePath());
        }
        return cache;
    }

    /**
     * 通过vFile构造cache的key
     * @param file 当前editor打开的虚拟文件(项目创建的code文件)
     * @return
     */
    public static String getCacheKeyByVFile(VirtualFile file) {
        String key = file.getPath();
        key = FileUtils.unifyPath(key);
        return key;
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

    public static LeetcodeEditor getLeetcodeEditorByEditor(SplitTextEditorWithPreview editor, Project project) {
        // get cache
        String path = editor.getFile().getPath();
        path = FileUtils.unifyPath(path);
        return StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
    }

    public static void showDialog(Project project, String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 通过vFile更新lc内容
     * @param file
     * @param lc
     */
    public static void updateLeetcodeEditorByVFile(Project project, VirtualFile file, LeetcodeEditor lc) {
        String key = getCacheKeyByVFile(file);
        StoreService.getInstance(project).addCache(key, lc);
    }
}
