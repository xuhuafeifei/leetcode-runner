package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.review.backend.model.QueryDimModel;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.MockRQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
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
    private boolean isShowingSolution = true;
    private JPanel contentPanel;
    private Component currentCard;

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

    private Dimension globalSize;

    private Component createQuestionCard(ReviewQuestion rq) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        // 创建内容面板容器
        contentPanel = new JPanel(new BorderLayout());
        this.currentCard = createEditorComponent(rq);  // 保存引用
        contentPanel.add(this.currentCard, BorderLayout.CENTER);
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
            // 获取当前Question面板的大小
            if (isShowingSolution) {
                // isShowingSolution为true, 表示此时面板是显示题解, 需要记录此时的大小
                globalSize = this.currentCard.getSize();
            }

            isShowingSolution = !isShowingSolution;
            // 更新按钮文字
            flipButton.setText(BundleUtils.i18nHelper(
                isShowingSolution ? "查看题目" : "查看题解",
                isShowingSolution ? "question" : "solution"
            ));
            

            // 替换编辑器组件
            contentPanel.removeAll();
            this.currentCard = createEditorComponent(rq);
            contentPanel.add(this.currentCard, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        bottomPanel.add(masterBtn);
        bottomPanel.add(continueBtn);
        bottomPanel.add(flipButton);

        jPanel.add(bottomPanel, BorderLayout.SOUTH);

        return jPanel;
    }


    private Component createEditorComponent(ReviewQuestion rq) {
        if (isShowingSolution) {
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
            var textArea = new JTextArea();
            textArea.setText(rq.getUserSolution());
            textArea.setEditable(true);
            textArea.setRows(13);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            
            // 设置边框和内边距
            textArea.setBorder(new CompoundBorder(
                JBUI.Borders.customLine(JBColor.border(), 1),  // 外边框
                JBUI.Borders.empty(8)  // 内边距
            ));
            
            // 设置背景色为稍微淡一点的主题色
            textArea.setBackground(JBColor.background().brighter());
            
            // 添加焦点监听器
            textArea.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        textArea.setCaretPosition(textArea.getText().length());
                    });
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    String newSolution = textArea.getText();
                    // TODO: 在这里实现保存逻辑
                    System.out.println("Save solution: " + newSolution);
                }
            });
            textArea.setSize(globalSize);

            return textArea;
        }
    }
}
