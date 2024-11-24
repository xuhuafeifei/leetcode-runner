package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.SubmissionEditor;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.Submission;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmissionService {
    public static void loadSolution(Project project, MyList<Submission> myList, String slug) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading...", false) {
            @Override
            public void run(ProgressIndicator indicator) {
                // query
                List<Submission> submissionList = LeetcodeClient.getInstance(project).getSubmissionList(slug);
                myList.setListData(submissionList);
                myList.updateUI();
            }
        });
    }

    /**
     * get code content and open content editor
     */
    public static void openSubmissionEditor(Project project, String id, SubmissionEditor solutionEditor) {
        String code = LeetcodeClient.getInstance(project).getSubmissionCode(id);
        solutionEditor.openSecond(code);
    }
}
