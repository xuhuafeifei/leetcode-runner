package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
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
public class DailyPlanTabPanel extends JPanel {
    private final Project project;
    private final ReviewQuestionService service;
    private List<ReviewQuestion> totalReviewQuestion;
    private Component questionCard;
    private boolean isShowingSolution = false;  // 添加状态标记
    private Component currentEditor; // 当前编辑器组件
    private JPanel contentPanel; // 内容面板

    public DailyPlanTabPanel(Project project) {
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
            this.questionCard = createQuestionCard(totalReviewQuestion.get(++cursor));
            panel.add(this.questionCard, BorderLayout.CENTER);
        }

        var lef = new JButton(IconLoader.getIcon("/icons/left-btn.svg", this.getClass()));
        lef.setBorder(BorderFactory.createEmptyBorder());
        lef.setContentAreaFilled(false);
        lef.addActionListener(e -> {
           if (cursor > 0) {
               if (this.questionCard != null) {
                   panel.remove(this.questionCard);
               }
               this.questionCard = createQuestionCard(totalReviewQuestion.get(--cursor));
               panel.add(this.questionCard, BorderLayout.CENTER);
               panel.revalidate();
               panel.repaint();
           }
        });

        var rig = new JButton(IconLoader.getIcon("/icons/right-btn.svg", this.getClass()));
        rig.setBorder(BorderFactory.createEmptyBorder());
        rig.setContentAreaFilled(false);
        rig.addActionListener(e -> {
            if (cursor < totalReviewQuestion.size() - 1) {
                if (this.questionCard != null) {
                    panel.remove(this.questionCard);
                }
                this.questionCard = createQuestionCard(totalReviewQuestion.get(++cursor));
                panel.add(this.questionCard, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        });

        panel.add(lef, BorderLayout.WEST);
        panel.add(rig, BorderLayout.EAST);

        add(panel);
    }

    private Component createQuestionCard(ReviewQuestion rq) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        // 创建内容面板容器
        contentPanel = new JPanel(new BorderLayout());
        currentEditor = createEditorPane(rq, true);
        isShowingSolution = !isShowingSolution;

        contentPanel.add(currentEditor, BorderLayout.CENTER);

        jPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        // 掌握button
        JButton masterBtn = new JButton(BundleUtils.i18nHelper("掌握", "master"));
        // 继续学习button
        JButton continueBtn = new JButton(BundleUtils.i18nHelper("继续学习", "continue"));
        // 反转button
        JButton flipButton = new JButton(BundleUtils.i18nHelper("查看题解", "solution"));

        // 添加反转按钮的点击事件
        flipButton.addActionListener(e -> {
            isShowingSolution = !isShowingSolution;
            // 更新按钮文字
            flipButton.setText(BundleUtils.i18nHelper(
                    isShowingSolution ? "查看题目" : "查看题解",
                    isShowingSolution ? "question" : "solution"
            ));

            // 移除当前编辑器
            contentPanel.removeAll();

            // 创建并添加新编辑器
            contentPanel.add(createEditorPane(rq, isShowingSolution), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        bottomPanel.add(masterBtn);
        bottomPanel.add(continueBtn);
        bottomPanel.add(flipButton);

        jPanel.add(bottomPanel, BorderLayout.SOUTH);

        return jPanel;
    }

    private Component createEditorPane(ReviewQuestion rq, boolean isSolution) {
        if (!isSolution) {
            // 题目模式 - 使用JEditorPane
            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setEditable(false);
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
        } else {
            // 题解模式 - 使用JTextArea
            var textArea = new JTextField();
            textArea.setText(rq.getUserSolution());
            textArea.setEnabled(true);
            textArea.setEditable(true);

            // 添加焦点监听器，在失去焦点时保存
            textArea.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    String newSolution = textArea.getText();
                    System.out.println("Saving solution: " + newSolution);
                }
            });

            return textArea;
        }
    }
}
