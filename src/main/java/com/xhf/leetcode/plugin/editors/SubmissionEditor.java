package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.listener.SubmissionListener;
import com.xhf.leetcode.plugin.model.Submission;
import com.xhf.leetcode.plugin.render.SubmissionCellRender;
import com.xhf.leetcode.plugin.service.SubmissionService;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmissionEditor extends AbstractSplitTextEditor {

    public SubmissionEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected void initFirstComp() {
        MyList<Submission> myList = new MyList<>();
        // make list can show content with multi-line
        myList.setCellRenderer(new SubmissionCellRender());
        // make list can interact with user and open to solution content by click
        myList.addMouseListener(new SubmissionListener(project, myList, this));
        myList.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 14));
        SubmissionService.loadSolution(project, myList, ViewUtils.getLeetcodeEditorByVFile(file, project).getTitleSlug());
        jbSplitter.setFirstComponent(new JBScrollPane(myList));
    }

    @Override
    public void openSecond(String content) {
        // build light virtual file
        LightVirtualFile submissionFile = new LightVirtualFile(
                ViewUtils.getLeetcodeEditorByVFile(file, project).getTitleSlug() + ".code", content
        );
        CodeEditor codeEditor = new CodeEditor(project, submissionFile);
        BorderLayoutPanel secondComponent = JBUI.Panels.simplePanel(codeEditor.getComponent());
        secondComponent.addToTop(createToolbarWrapper(codeEditor.getComponent()));
        jbSplitter.setSecondComponent(secondComponent);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Submission Editor";
    }
}
