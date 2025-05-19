package com.xhf.leetcode.plugin.review.utils;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.front.ReviewEnv;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ReviewUtils {

    public static void doIt(ReviewQuestion rq, Project project, ReviewEnv env) {
        // 获取当前Question
        Question q = QuestionService.getInstance(project).getTotalQuestion(project).get(rq.getId());
        try {
            CodeService.getInstance(project).openCodeEditor(q);
        } catch (FileCreateError e) {
            ConsoleUtils.getInstance(project).showError(BundleUtils.i18nHelper("打开文件失败", "Failed to open file"), true, true, e.getMessage(), "Error", null);
            LogUtils.warn(e);
            return;
        }
        // 关闭当前窗口
        env.post("close_window");
    }

}
