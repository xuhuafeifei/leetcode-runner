package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class HTMLEditor implements FileEditor {
    private Project project;
    private LightVirtualFile vFile;
    private JCEFHtmlPanel jcefHtmlPanel;
    // default template
    private final String templatePath = "\\template\\default.html";
    private final BorderLayoutPanel borderLayoutPanel;


    public HTMLEditor(@NotNull Project project, @NotNull LightVirtualFile file) {
        this.project = project;
        this.vFile = file;
        this.jcefHtmlPanel = new JCEFHtmlPanel(this.vFile.getUrl());
        this.jcefHtmlPanel.loadHTML(loadHTMLContent());
        this.borderLayoutPanel = JBUI.Panels.simplePanel();

        this.borderLayoutPanel.addToCenter(jcefHtmlPanel.getComponent());
    }

    private String loadHTMLContent() {
        URL url = FileUtils.getResourceFileUrl(this.templatePath);
        String html = FileUtils.readContentFromFile(url);
        // handle html
        String newHtml = html.replace("{{content}}", vFile.getContent());
        return newHtml;
    }

    @Override
    public @NotNull JComponent getComponent() {
        // return jcefHtmlPanel.getComponent();
        return this.borderLayoutPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return jcefHtmlPanel.getComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "markdown";
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
        jcefHtmlPanel.dispose();
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }
}
