package com.xhf.leetcode.plugin.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.testFramework.LightVirtualFile;
import com.xhf.leetcode.plugin.bus.RePositionEvent;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.DeepCodingInfo;
import com.xhf.leetcode.plugin.model.DeepCodingQuestion;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

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

    public static LeetcodeEditor getLeetcodeEditorByCurrentVFile(Project project) {
        VirtualFile cFile = getCurrentOpenVirtualFile(project);
        return getLeetcodeEditorByVFile(cFile, project);
    }

    /**
     * 通过vFile构造cache的key
     * @param file 当前editor打开的虚拟文件(项目创建的code文件)
     * @return
     */
    public static String getCacheKeyByVFile(VirtualFile file) {
        if (file == null) {
            return null;
        }
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
     *
     * @param file
     * @param lc
     * @return
     */
    public static boolean updateLeetcodeEditorByVFile(Project project, VirtualFile file, LeetcodeEditor lc) {
        String key = getCacheKeyByVFile(file);
        StoreService.getInstance(project).addCache(key, lc);
        return true;
    }

    /**
     * 获取当前打开的虚拟文件
     * @param project project
     * @return 虚拟文件
     */
    public static VirtualFile getCurrentOpenVirtualFile(Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
        if (selectedFiles.length > 0) {
            return selectedFiles[0];
        }
        return null;
    }

    /**
     * 打开当前显示的file editor
     * @param project
     * @return
     */
    public static FileEditor getCurrentOpenEditor(Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor[] selectedEditors = fileEditorManager.getSelectedEditors();
        if (selectedEditors.length > 0) {
            return selectedEditors[0];
        }
        return null;
    }

    public static String getUnifyFilePathByVFile(VirtualFile file) {
        String key = file.getPath(); // file separator is /
        key = FileUtils.unifyPath(key);
        return key;
    }

    public static void closeVFile(VirtualFile virtualFile, Project project) {
        if (project != null && virtualFile != null) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            fileEditorManager.closeFile(virtualFile);
        }
    }

    public static void closeCVFile(Project project) {
        closeVFile(getCurrentOpenVirtualFile(project), project);
    }

    /**
     * 将内容写入vFile
     * @param cFile 要写入的虚拟文件
     * @param content 要写入的内容
     * @return 是否写入成功
     */
    public static boolean writeContentToVFile(VirtualFile cFile, String content) {
        Application application = ApplicationManager.getApplication();
        AtomicReference<Boolean> result = new AtomicReference<>(true);
        application.invokeAndWait(() -> {
            // 获取文件的文档
            Document document = FileDocumentManager.getInstance().getDocument(cFile);
            if (document != null) {
                application.invokeAndWait(() -> {
                    application.runWriteAction(() -> {
                        document.setText(content);
                    });
                });
            } else {
                // 如果无法获取文档，则使用OutputStream写入文件
                try (OutputStream outputStream = cFile.getOutputStream(CodeService.class)) {
                    outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignored) {
                    result.set(false);
                }
            }
        });
        return result.get();
    }

    /**
     * 将内容写入当前打开的虚拟文件
     * @param project project
     * @param content 写入内容
     * @return 是否写入成功
     */
    public static boolean writeContentToCurrentVFile(@Nullable Project project, String content) {
        VirtualFile cFile = getCurrentOpenVirtualFile(project);
        return writeContentToVFile(cFile, content);
    }

    public static boolean updateLeetcodeEditorByCurrentVFile(Project project, LeetcodeEditor lc) {
        VirtualFile cFile = getCurrentOpenVirtualFile(project);
        if (cFile == null) return false;
        return updateLeetcodeEditorByVFile(project, cFile, lc);
    }

    public static String getContentFromVFile(VirtualFile file) {
        // 获取当前打开的 VirtualFile
        if (file == null) {
            return null;
        }
        // 获取文件的 Document
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        // 获取文件内容
        return document.getText();
    }

    public static String getContentOfCurrentOpenVFile(Project project) {
        // 获取当前打开的 VirtualFile
        VirtualFile currentFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (currentFile == null) {
            return null;
        }
        // 获取文件的 Document
        AtomicReference<Document> document = new AtomicReference<>(null);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            document.set(FileDocumentManager.getInstance().getDocument(currentFile));
        });
        Document res = document.get();
        if (res == null) {
            return null;
        }
        // 获取文件内容
        return res.getText();
    }

    /**
     * 选择并显示myList第i个元素
     * @param myList myList
     * @param i idx
     */
    public static void scrollToVisibleOfMyList(MyList<?> myList, int i) {
        // 选择匹配到的题目
        myList.setSelectedIndex(i);
        // 滚动到选中的题目位置
        Rectangle cellRect = myList.getCellBounds(i, i);
        if (cellRect != null) {
            myList.scrollRectToVisible(cellRect);
        }
    }

    /**
     * 选择并显示myList第i个元素
     * @param myList myList
     * @param i idx
     */
    public static void scrollToVisibleOfMyList(MyList<?> myList, int i, boolean asyn) {
        if (asyn) {
            ApplicationManager.getApplication().invokeLater(() -> {
                scrollToVisibleOfMyList(myList, i);
            });
        } else {
            scrollToVisibleOfMyList(myList, i);
        }
    }

    /**
     * deep coding模式下的重定位
     *
     * @param event event
     * @param questionList questionList
     * @param project project
     * @param pattern deepcoding的对应模式
     */
    public static void rePositionInDeepCoding(RePositionEvent event, MyList<? extends DeepCodingQuestion> questionList, Project project, String pattern) {
        String fid = event.getFrontendQuestionId();
        String titleSlug = event.getTitleSlug();
        ListModel<?> model = questionList.getModel();
        int size = model.getSize();

        try {
            int i = Integer.parseInt(fid) - 1;
            DeepCodingQuestion question;

            // 尝试根据fid直接定位
            if (i < size && (question = (DeepCodingQuestion) model.getElementAt(i)).getTitleSlug().equals(titleSlug)) {
                handleSuccessfulReposition(question, size, i, questionList, event, project, pattern);
            } else {
                //如果fid匹配失败，遍历model查找
                for (int j = 0; j < size; j++) {
                    question = (DeepCodingQuestion) model.getElementAt(j);
                    if (question.getTitleSlug().equals(titleSlug)) {
                        handleSuccessfulReposition(question, size, j, questionList, event, project, pattern);
                        return;
                    }
                }
                JOptionPane.showMessageDialog(null, "当前文件无法被重新打开");
            }
        } catch (Exception ex) {
            LogUtils.error(ex);
            ConsoleUtils.getInstance(project).showError("文件重定位错误! 错误原因是: " + ex.getMessage(), false, true);
        }
    }

    /**
     * 根据系统设置的langType打开文件
     * @param question question
     * @param size 当前deepcoding模式下显示的有多少题目
     * @param index 当前题目的index
     * @param questionList questionList
     * @param event event
     * @param project project
     * @param pattern deepcoding 的那种模式
     */
    private static void handleSuccessfulReposition(DeepCodingQuestion question, int size, int index, MyList<? extends DeepCodingQuestion> questionList, RePositionEvent event, Project project, String pattern) {
        ViewUtils.scrollToVisibleOfMyList(questionList, index);
        TaskCenter.getInstance().createTask(() -> {
            // 重新打开文件
            DeepCodingInfo hot1001 = new DeepCodingInfo(pattern, size, index);
            try {
                CodeService.getInstance(project).reOpenCodeEditor(question.toQuestion(project), event.getFile(), event.getLangType(), hot1001);
            } catch (FileCreateError e) {
                LogUtils.error(e);
                ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("code.service.file.create.error") + "\n" + e.getMessage(), true, true);
                return;
            }

            // 获取并显示 ToolWindow（确保控制台窗口可见）
            showToolWindow(project);
        }).invokeLater();
        // JOptionPane.showMessageDialog(null, "重定位成功! 当前文件将立刻被重新打开");
    }


    private static void showToolWindow(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
                ToolWindow toolWindow = toolWindowManager.getToolWindow(LCToolWindowFactory.LEETCODE_RUNNER_ID);
                if (toolWindow != null && !toolWindow.isVisible()) {
                    toolWindow.show();  // 显示控制台窗口
                }
            } catch (Exception e) {
                // 如果发生异常, 不要影响用户使用, 该功能不是必须的
                LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            }
        });
    }

    public static void rePosition(RePositionEvent event, MyList<Question> questionList, Project project) {
        String fid = event.getFrontendQuestionId();
        String titleSlug = event.getTitleSlug();
        ListModel<Question> model = questionList.getModel();
        try {
            int i = Integer.parseInt(fid) - 1;
            Question question;
            // double check
            if (
                    i < model.getSize() &&
                    (question = model.getElementAt(i)).getTitleSlug().equals(titleSlug))
            {
                ViewUtils.scrollToVisibleOfMyList(questionList, i);
                TaskCenter.getInstance().createTask(() -> {
                    // 重新打开文件 (根据当前系统语言打开文件)
                    try {
                        CodeService.getInstance(project).reOpenCodeEditor(question, event.getFile(), event.getLangType());
                    } catch (FileCreateError e) {
                        LogUtils.error(e);
                        ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("code.service.file.create.error") + "\n" + e.getMessage(), true, true);
                        return;
                    }
                    // 获取并显示 ToolWindow（确保控制台窗口可见）
                    showToolWindow(project);
                }).invokeLater();
                // JOptionPane.showMessageDialog(null, "重定位成功! 当前文件将立刻被重新打开");
                return;
            }
            JOptionPane.showMessageDialog(null, "当前文件无法被重新打开");
        } catch (Exception ex) {
            LogUtils.error(ex);
            ConsoleUtils.getInstance(project).showError("文件重定位错误! 错误原因是: " + ex.getMessage(), false, true);
        }
    }
}
