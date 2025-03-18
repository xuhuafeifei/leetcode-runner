package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.review.backend.model.QueryDimModel;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.MockRQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RecordTabPanel extends JPanel {
    private final Project project;
    private final ReviewQuestionService service;

    public RecordTabPanel(Project project) {
        this.project = project;
        this.service = MockRQServiceImpl.getInstance();

        loadContent();
    }

    private void loadContent() {
        List<ReviewQuestion> totalReviewQuestion = service.getTotalReviewQuestion(new QueryDimModel());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (ReviewQuestion rq : totalReviewQuestion) {
            panel.add(createLine(rq));
        }
        // 为panel添加滚轮
        var scrollPane = new JBScrollPane(panel);
        add(scrollPane);
    }

    private Component createLine(ReviewQuestion rq) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.add(new JBLabel(rq.toString()));
        jPanel.add(new JButton(BundleUtils.i18n("action.leetcode.review.delete")));
        jPanel.add(new JButton(BundleUtils.i18n("action.leetcode.review.continue")));
        return jPanel;
    }
}
