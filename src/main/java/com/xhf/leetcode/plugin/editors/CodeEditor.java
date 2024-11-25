package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.openapi.util.Key;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeEditor implements FileEditor {
    private Project project;
    private LightVirtualFile vFile;
    private JTextPane textPane;
    private final BorderLayoutPanel borderLayoutPanel;


    /**
     * support copy code ability
     */
    protected final SplitEditorToolbar createToolbarWrapper(JComponent comp) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(copyAction(), copyToContentEditor());
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Solution", actionGroup, true);
        actionToolbar.setTargetComponent(comp);
        return new SplitEditorToolbar(null, actionToolbar);
    }

    private AnAction copyAction() {
        return new AnAction("Copy", "Copy", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // copy to clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(textPane.getText());
                clipboard.setContents(stringSelection, null);

                JOptionPane.showMessageDialog(null, "Copy Success");
            }
        };
    }

    private AnAction copyToContentEditor() {
        return new AnAction("Copy to Content Editor", "Copy to content editor", AllIcons.Actions.Back) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // todo: copy to clipboard and paste code to editor
                JOptionPane.showMessageDialog(null, "Not Supported Yet.");
            }
        };
    }

    public CodeEditor(@NotNull Project project, @NotNull LightVirtualFile file) {
        this.project = project;
        this.vFile = file;
        this.textPane = new JTextPane();
        this.textPane.setText(String.valueOf(vFile.getContent())); // set history code to text pane
        this.textPane.setFont(new Font("Courier New", Font.PLAIN, 15));
        this.textPane.setForeground(new Color(110, 107, 107));
        this.textPane.setBackground(new Color(253, 255, 255));
        this.textPane.setEditable(false);
        this.borderLayoutPanel = JBUI.Panels.simplePanel();
        this.borderLayoutPanel.addToTop(createToolbarWrapper(this.textPane));
        JBScrollPane jsp = new JBScrollPane(this.textPane);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.borderLayoutPanel.addToCenter(jsp);
    }

    @Override
    public @NotNull JComponent getComponent() {
        // return jcefHtmlPanel.getComponent();
        return this.borderLayoutPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.borderLayoutPanel;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Code Editor";
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
    public void dispose() {
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }
}
