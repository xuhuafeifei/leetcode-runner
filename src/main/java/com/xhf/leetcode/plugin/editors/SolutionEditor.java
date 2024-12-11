package com.xhf.leetcode.plugin.editors;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.listener.SolutionListener;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.render.SolutionCellRenderer;
import com.xhf.leetcode.plugin.service.SolutionService;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
// 订阅ClearCacheEvent, 当接受该消息时，会执openError()方法, 清除显示内容
@LCSubscriber(events = {ClearCacheEvent.class})
public class SolutionEditor extends AbstractSplitTextEditor {

    public SolutionEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    /**
     * 创建打开错误提示
     */
    private void openError() {
        JTextPane jTextPane = showNotingTextPane();
        jTextPane.setText(Constants.SOLUTION_OPEN_ERROR);
        jTextPane.setForeground(Constants.RED_COLOR);
        BorderLayoutPanel secondComponent = JBUI.Panels.simplePanel(jTextPane);
        jbSplitter.setFirstComponent(secondComponent);
        jbSplitter.setSecondComponent(null);
    }

    @Override
    protected void initFirstComp() {
        MyList<Solution> myList = new MyList<>();
        // make list can show content with multi-line
        myList.setCellRenderer(new SolutionCellRenderer<Solution>());
        // make list can interact with user and open to solution content by click
        myList.addMouseListener(new SolutionListener(project, myList, this));
        myList.setFont(Constants.CN_FONT);
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);
        if (lc == null) {
            openError();
            return;
        }
        SolutionService.loadSolution(project, myList, lc.getTitleSlug());
        jbSplitter.setFirstComponent(new JBScrollPane(myList));
    }

    @Override
    public void openSecond(String content) {
        // build light virtual file
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);
        BorderLayoutPanel secondComponent;
        if (lc == null) {
            openError();
            return;
        }
        LightVirtualFile solutionFile = new LightVirtualFile(
                lc.getTitleSlug()+ ".solution.md", content
        );
        if (StringUtils.isBlank(content)) {
            JTextPane jTextPane = showNotingTextPane();
            jTextPane.setText(Constants.SOLUTION_CONTENT_NOT_SUPPORT);
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

    @Subscribe
    public void clearCacheListener(ClearCacheEvent event) {
        openError();
    }
}
