package com.xhf.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.SubmissionEditor;
import com.xhf.leetcode.plugin.model.Submission;
import com.xhf.leetcode.plugin.service.SubmissionService;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmissionListener extends AbstractMouseAdapter {
    private final MyList<Submission> submissionList;
    private final SubmissionEditor solutionEditor;

    public SubmissionListener(Project project, MyList<Submission> submissionList, SubmissionEditor solutionEditor) {
        super(project);
        this.submissionList = submissionList;
        this.solutionEditor = solutionEditor;
    }

    /**
     * 打开提交记录
     * @param e
     */
    @Override
    protected void doubleClicked(MouseEvent e) {
        Point point = e.getPoint();
        int idx = submissionList.locationToIndex(point);
        Submission submission = submissionList.getModel().getElementAt(idx);
        SubmissionService.openSubmissionEditor(project, submission.getId(), solutionEditor);
    }
}