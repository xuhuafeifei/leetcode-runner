package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.xhf.leetcode.plugin.review.backend.model.QueryDimModel;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.MockRQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;

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
    private List<ReviewQuestion> totalReviewQuestion;

    public RecordTabPanel(Project project) {
        this.project = project;
        this.service = MockRQServiceImpl.getInstance();

        loadContent();
    }

    int cursor = -1;

    private void loadContent() {
        this.totalReviewQuestion = service.getTotalReviewQuestion(new QueryDimModel());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        if (! totalReviewQuestion.isEmpty()) {
            Component questionCard = createQuestionCard(totalReviewQuestion.get(++cursor));
            panel.add(questionCard, BorderLayout.CENTER);
        }

        var lef = new JButton(IconLoader.getIcon("/icons/left-btn.svg", this.getClass()));
        lef.setBorder(BorderFactory.createEmptyBorder());
        lef.setContentAreaFilled(false);
        lef.addActionListener(e -> {
           if (cursor > 0) {
               panel.add(createQuestionCard(totalReviewQuestion.get(--cursor)), BorderLayout.CENTER);
               panel.revalidate();
               panel.repaint();
           }
        });

        var rig = new JButton(IconLoader.getIcon("/icons/right-btn.svg", this.getClass()));
        rig.setBorder(BorderFactory.createEmptyBorder());
        rig.setContentAreaFilled(false);
        rig.addActionListener(e -> {
            if (cursor < totalReviewQuestion.size() - 1) {
                panel.add(createQuestionCard(totalReviewQuestion.get(++cursor)), BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        });

        panel.add(lef, BorderLayout.WEST);
        panel.add(rig, BorderLayout.EAST);

        add(panel);
    }

    private Component createQuestionCard(ReviewQuestion rq) {
        // 未开始
        // 题目编号
        // 题目名称
        // 题目难度 + 用户评价
        // 上次做题时间
        // 复习时间
        var editorPane = new JTextPane();
        editorPane.setContentType("text/html");
        editorPane.setText(
                new CssBuilder()
                .addStatus(rq.getStatus())
                .addTitle(rq.getTitle(), rq.getDifficulty())
                .addUserRate(rq.getUserRate())
                .lastModify(rq.getLastModify())
                .nextReview(rq.getNextReview())
                .build()
        );

        return editorPane;
    }
}
