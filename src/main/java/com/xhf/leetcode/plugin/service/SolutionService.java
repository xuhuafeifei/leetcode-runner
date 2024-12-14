package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.SolutionEditor;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SolutionService {
    /**
     * 这块就不搞什么事件解耦了, 太麻烦了. 而且这块业务少, 数据量也少, 没必要
     * @param project
     * @param myList
     * @param titleSlug
     */
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
     * @param project project
     * @param solution 需要打开的solution的相关信息
     * @param solutionEditor 用于显示solution的editor
     */
    public static void openSolutionContent(Project project, Solution solution, SolutionEditor solutionEditor) {
        String solutionContent = LeetcodeClient.getInstance(project).getSolutionContent(solution.getSlug());

        // 向lc存入solution相关信息
        VirtualFile file = solutionEditor.getFile();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);
        // 添加solution相关信息, 为后续创建solution提供数据支持
        if (lc != null) {
            lc.setTopicId(solution.getTopic().getId());
            lc.setSolutionSlug(solution.getSlug());
        }
        // 更新lc内容
        ViewUtils.updateLeetcodeEditorByVFile(project, file, lc);
        solutionEditor.openSecond(solutionContent);
    }
}
