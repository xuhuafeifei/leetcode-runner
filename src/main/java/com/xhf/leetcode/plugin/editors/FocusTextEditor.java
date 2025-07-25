package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.MarkdownContentType;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class FocusTextEditor implements FileEditor {

    /**
     * idea项目对象
     */
    private final Project project;
    /**
     * 打开的文件
     */
    private final VirtualFile file;
    /**
     * 核心容器
     */
    private JComponent myComponent;

    public FocusTextEditor(Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;
        initComponent();
    }

    /**
     * 初始化三个tab
     */
    private void initComponent() {
        JBTabbedPane tabbedPane = new JBTabbedPane();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);
        // lc检测. lc存储了service和editor之间沟通的重要数据, 这些数据会在editor显示内容是使用
        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(BundleUtils.i18n("editor.focus.open.error"), true, true);
            return;
        }
        // 参数检测
        if (StringUtils.isBlank(lc.getFrontendQuestionId()) ||
            StringUtils.isBlank(lc.getTranslatedTitle()) ||
            StringUtils.isBlank(lc.getDifficulty()) ||
            StringUtils.isBlank(lc.getTitleSlug()) ||
            StringUtils.isBlank(lc.getMarkdownContent())
        ) {
            ConsoleUtils.getInstance(project).showWaring(BundleUtils.i18n("editor.focus.open.error"), true, true);
            String s = BundleUtils.i18n("editor.focus.open.error.desc") + GsonUtils.toJsonStr(lc);
            LogUtils.error(s);
            ConsoleUtils.getInstance(project).showError(s, false);
            return;
        }
        JComponent contentPanel = new MarkDownEditor(project, buildMarkDownContent(lc),
            MarkdownContentType.QUESTION).getComponent();
        JComponent solutionPanel = new SolutionEditor(project, file).getComponent();
        JComponent submissionPanel = new SubmissionEditor(project, file).getComponent();

        tabbedPane.addTab(BundleUtils.i18n("editor.focus.tab.question"), contentPanel);
        tabbedPane.addTab(BundleUtils.i18n("editor.focus.tab.solution"), solutionPanel);
        tabbedPane.addTab(BundleUtils.i18n("editor.focus.tab.submission"), submissionPanel);

        myComponent = tabbedPane;
    }

    private Map<String, Object> buildMarkDownContent(LeetcodeEditor lc) {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FRONTEND_QUESTION_ID, lc.getFrontendQuestionId());
        map.put(Constants.TRANSLATED_TITLE, lc.getTranslatedTitle());
        map.put(Constants.DIFFICULTY, lc.getDifficulty());
        map.put(Constants.TITLE_SLUG, lc.getTitleSlug());
        map.put(Constants.QUESTION_CONTENT, lc.getMarkdownContent());
        map.put(Constants.VFILE, this.file);
        map.put(Constants.STATUS, lc.getStatus());
        map.put(Constants.IS_PAID_ONLY, lc.getIsPaidOnly());
        return map;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return myComponent;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return null;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    /**
     * 兼容不同版本, 早期版本super没有实现该方法
     *
     * @return null
     */
    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }
}
