package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.review.backend.model.QueryDimModel;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.MockRQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import javax.swing.border.Border;
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
//        for (ReviewQuestion rq : totalReviewQuestion) {
//            panel.add(createLine(rq));
//        }
        // 未开始
        // 题目编号
        // 题目名称
        // 题目难度 + 用户评价
        // 上次做题时间
        // 复习时间
        var editorPane = new JTextPane();
        editorPane.setContentType("text/html");
        editorPane.setText(new CssBuilder()
                .addStatus("逾期")
                .addTitle("[8] 两数之和", "EASY")
                .addUserRate("很难")
                .lastModify("2023/03/17")
                .nextReview("2023/03/18")
                .build()
        );
        panel.add(editorPane);

        // 为panel添加滚轮
        var scrollPane = new JBScrollPane(panel);
        add(scrollPane);
    }

    private Component createLine(ReviewQuestion rq) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText("Hello, <font color='red'>World!</font>");
        editorPane.setEditable(false);

        Border innerBorder = BorderFactory.createLineBorder(new Color(10, 20, 23), 1);
        Border padding = BorderFactory.createEmptyBorder(1, 6, 1, 6);
        editorPane.setBorder(BorderFactory.createCompoundBorder(padding, innerBorder));

        jPanel.add(editorPane);
        jPanel.add(new JButton(BundleUtils.i18n("action.leetcode.review.delete")));
        jPanel.add(new JButton(BundleUtils.i18n("action.leetcode.review.continue")));
        return jPanel;
    }
}
