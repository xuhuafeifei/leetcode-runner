package com.xhf.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.SolutionEditor;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.service.SolutionService;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SolutionListener extends AbstractMouseAdapter {
    private final MyList<Solution> solutionList;
    private final SolutionEditor solutionEditor;

    public SolutionListener(Project project, MyList<Solution> solutionList, SolutionEditor solutionEditor) {
        super(project);
        this.solutionList = solutionList;
        this.solutionEditor = solutionEditor;
    }

    /**
     * 打开题解
     * @param e
     */
    @Override
    protected void doubleClicked(MouseEvent e) {
        Point point = e.getPoint();
        int idx = solutionList.locationToIndex(point);
        Solution solution = solutionList.getModel().getElementAt(idx);
        SolutionService.openSolutionContent(project, solution, solutionEditor);
    }
}
