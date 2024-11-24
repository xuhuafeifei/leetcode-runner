package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.SolutionEditor;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.Solution;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SolutionService {
    public static void loadSolution(Project project, MyList<Solution> myList, String titleSlug) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading...", false) {
            @Override
            public void run(ProgressIndicator indicator) {
                // query
                List<Solution> solutionList = LeetcodeClient.getInstance(project).querySolutionList(titleSlug);
                myList.setListData(solutionList);
                myList.updateUI();
            }
        });
    }

    /**
     * get solution content and open content editor
     * @param project
     * @param solutionSlug
     * @param solutionEditor
     */
    public static void openSolutionContent(Project project, String solutionSlug, SolutionEditor solutionEditor) {
        String solutionContent = LeetcodeClient.getInstance(project).getSolutionContent(solutionSlug);
        solutionEditor.openSecond(solutionContent);
    }
}
