package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Map;

/**
 * 抽象分割文本编辑器: 提供分屏显示功能. 当前版本主要用于CodeEditor 和 SolutionEditor的内容构建
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractSplitTextEditor implements FileEditor {
    /**
     * idea核心工程对象
     */
    protected Project project;
    /**
     * 维护的核心面板组件
     */
    protected JComponent myComponent;
    /**
     * editor目前打开的文件
     */
    protected VirtualFile file;
    /**
     * 分割器
     */
    protected JBSplitter jbSplitter;

    public AbstractSplitTextEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        initComponent();
    }

    private void initComponent() {
        jbSplitter = new JBSplitter();
        // set first
        initFirstComp();
        myComponent = JBUI.Panels.simplePanel(jbSplitter);
    }

    /**
     * 当拆分器（splitter）的第二个编辑器可见时，工具栏将会显示在第二个编辑器中，并且该工具栏包含关闭操作
     * <p>
     * 如果激活了关闭操作，第二个编辑器将会被关闭
     * <p>
     * 该方法返回一个指示是否成功关闭第二个编辑器的布尔值。
     *
     * @return 返回Toolbar工具栏
     */
    protected final SplitEditorToolbar createToolbarWrapper(JComponent comp) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(new AnAction("Close Solution", "Close solution", AllIcons.Actions.Close) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                closeSplitEditor();
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Solution", actionGroup, true);
        actionToolbar.setTargetComponent(comp);
        return new SplitEditorToolbar(null, actionToolbar);
    }

    private void closeSplitEditor() {
        jbSplitter.getSecondComponent().setVisible(false);
    }

    /**
     * 初始化分割器的第一块面板的内容
     */
    protected abstract void initFirstComp();

    @Override
    public final @NotNull JComponent getComponent() {
        return myComponent;
    }

    protected JTextPane showNotingTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 15));
        textPane.setForeground(new JBColor(Color.darkGray, new Color(253, 255, 255)));
        textPane.setBackground(new JBColor(new Color(253, 255, 255), Color.darkGray));
        textPane.setEditable(false);
        return textPane;
    }

    /**
     * support open content ability
     * 为了拓展content提供的信息, 采用Map封装数据
     */
    public abstract void openSecond(Map<String, Object> content);

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
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
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    @Override
    public void dispose() {

    }
}
