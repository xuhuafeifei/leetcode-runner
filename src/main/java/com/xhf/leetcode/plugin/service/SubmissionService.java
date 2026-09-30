package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.SubmissionEditor;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.Submission;
import com.xhf.leetcode.plugin.model.SubmissionDetail;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmissionService {

    public static void loadSubmission(Project project, MyList<Submission> myList, String slug) {
        // 网络请求放到后台线程, 避免阻塞 EDT (曾导致 IDE 冻结 14s)
        // 查询完成后通过 invokeLater 回到 EDT 更新 UI
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<Submission> submissionList;
            try {
                submissionList = LeetcodeClient.getInstance(project).getSubmissionList(slug);
            } catch (Exception e) {
                LogUtils.error("loadSubmission failed: " + e.getMessage());
                submissionList = new ArrayList<>();
            }
            List<Submission> finalList = submissionList;
            ApplicationManager.getApplication().invokeLater(() -> {
                myList.setListData(finalList);
                myList.updateUI();
            });
        });
    }

    /**
     * get code content and open content editor
     */
    public static void openSubmissionEditor(Project project, String id, SubmissionEditor submissionEditor) {
        SubmissionDetail sd = LeetcodeClient.getInstance(project).getSubmissionDetail(id);
        // 缓存submission相关数据
        StoreService.getInstance(project).addCache(id, sd, false);
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.SUBMISSION_ID, id);
        // solution通过id获取submission详细数据信息, 然后构建内容
        submissionEditor.openSecond(map);
    }
}