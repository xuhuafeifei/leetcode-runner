package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.listener.SolutionListener;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.render.SolutionCellRenderer;
import com.xhf.leetcode.plugin.service.SolutionService;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SolutionEditor extends AbstractSplitTextEditor {

    public SolutionEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected void initFirstComp() {
        MyList<Solution> myList = new MyList<>();
        // make list can show content with multi-line
        myList.setCellRenderer(new SolutionCellRenderer<Solution>());
        // make list can interact with user and open to solution content by click
        myList.addMouseListener(new SolutionListener(project, myList, this));
        myList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        SolutionService.loadSolution(project, myList, ViewUtils.getLeetcodeEditorByVFile(file, project).getTitleSlug());
        jbSplitter.setFirstComponent(new JBScrollPane(myList));
    }

    @Override
    public void openSecond(String content) {
        // build light virtual file
        LightVirtualFile solutionFile = new LightVirtualFile(
                ViewUtils.getLeetcodeEditorByVFile(file, project).getTitleSlug()+ ".solution.md", content
        );
        BorderLayoutPanel secondComponent = null;
        if (StringUtils.isBlank(content)) {
            JTextPane jTextPane = showNotingTextPane();
            jTextPane.setText("This content is not supported to show yet..., please choose another solution");
            secondComponent = JBUI.Panels.simplePanel(jTextPane);
            secondComponent.addToTop(createToolbarWrapper(jTextPane));
        }else {
            MarkDownEditor markDownEditor = new MarkDownEditor(project, solutionFile);
            secondComponent = JBUI.Panels.simplePanel(markDownEditor.getComponent());
            secondComponent.addToTop(createToolbarWrapper(markDownEditor.getComponent()));
        }
        jbSplitter.setSecondComponent(secondComponent);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return myComponent;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Solution Editor";
    }

}