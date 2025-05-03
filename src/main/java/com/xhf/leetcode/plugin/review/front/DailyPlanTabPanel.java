package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.RQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.review.utils.AbstractMasteryDialog;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

/**
 * 每日一题面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DailyPlanTabPanel extends JPanel {
    private final Project project;

    private final ReviewQuestionService service;

    private final ReviewEnv env;

    /**
     * 卡片面板, 包含内容+底部button
     */
    private Component questionCard;
    private boolean isShowingSolution = true;
    private JPanel contentPanel;

    /**
     * 当前面板, 只包含内容信息
     */
    private Component currentCard;
    private JPanel questionCardPanel;

    private Dimension globalSize;
    private @Nullable ReviewQuestion topQuestion;

    public DailyPlanTabPanel(Project project, ReviewEnv env) {
        this.project = project;
        this.service = new RQServiceImpl(project);
        this.env = env;
        this.env.registerListener(this::onMessageReceived);
        loadContent();
    }

    private void onMessageReceived(String msg) {
        if ("get_top_question".equals(msg)) {
            nextQuestion();
        }
    }

    private void loadContent() {
        this.questionCardPanel = new JPanel();
        questionCardPanel.setLayout(new BorderLayout());

        this.topQuestion = service.getTopQuestion();
        this.questionCard = createQuestionCard(topQuestion);
        questionCardPanel.add(this.questionCard, BorderLayout.CENTER);

        add(questionCardPanel);
    }


    private Component createQuestionCard(ReviewQuestion rq) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        // 创建内容面板容器
        contentPanel = new JPanel(new BorderLayout());
        if (rq != null) {
            this.currentCard = createEditorComponent(rq);  // 保存引用
            contentPanel.add(this.currentCard, BorderLayout.CENTER);
            jPanel.add(contentPanel, BorderLayout.CENTER);

            JPanel bottomPanel = getjPanel(rq);

            jPanel.add(bottomPanel, BorderLayout.SOUTH);
        } else {
            JEditorPane emptyMessagePane = new JEditorPane();
            emptyMessagePane.setContentType("text/html");
            emptyMessagePane.setEditable(false);

            String message = BundleUtils.i18nHelper("没有题目需要复习！", "No question needs to be reviewed!");

            String htmlContent = createHtmlContent().replace("${message}", message);

            this.currentCard = emptyMessagePane;
            emptyMessagePane.setText(htmlContent);

            jPanel.add(emptyMessagePane, BorderLayout.CENTER);
        }

        return jPanel;
    }

    private @NotNull JPanel getjPanel(ReviewQuestion rq) {
        JPanel bottomPanel = new JPanel();
        // 掌握button
        JButton masterBtn = new JButton(BundleUtils.i18nHelper("掌握", "master"));
        masterBtn.addActionListener(e -> showMasteryDialog());

        // 继续学习button
        JButton deleteBtn = new JButton(BundleUtils.i18nHelper("删除", "delete"));
        deleteBtn.addActionListener(e -> removeQuestion(rq));

        // 解题button
        JButton doItBtn = new JButton(BundleUtils.i18nHelper("做题", "do it"));
        doItBtn.addActionListener(e -> doIt(rq));

        // 反转button
        // JButton flipButton = getFlipBtn(rq);

        bottomPanel.add(masterBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(doItBtn);
        return bottomPanel;
    }

    private void doIt(ReviewQuestion rq) {
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

    private String createHtmlContent() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        .empty-message {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            text-align: center;\n" +
                "            padding: 40px;\n" +
                "            color: #666;\n" +
                "            font-size: 18px;\n" +
                "            background-color: #f8f9fa;\n" +
                "            border-radius: 8px;\n" +
                "            margin: 20px;\n" +
                "            box-shadow: 0 2px 4px rgba(0,0,0,0.05);\n" +
                "        }\n" +
                "        .empty-message .icon {\n" +
                "            font-size: 48px;\n" +
                "            margin-bottom: 15px;\n" +
                "            color: #adb5bd;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"empty-message\">\n" +
                "        <div class=\"icon\">☑\uFE0F</div>\n" +
                "        <div>${message}</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 反转按钮, 用于切换题目 -> 题解, 题解 -> 题目
     * @param rq question
     * @return JButton
     */
    private @NotNull JButton getFlipBtn(ReviewQuestion rq) {
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
        return flipButton;
    }

    private void removeQuestion(ReviewQuestion rq) {
        service.deleteQuestion(rq.getId());

        // 刷新UI
        this.questionCardPanel.remove(this.questionCard);
        this.topQuestion = service.getTopQuestion();
        this.questionCard = createQuestionCard(this.topQuestion);

        questionCardPanel.add(this.questionCard, BorderLayout.CENTER);
        questionCardPanel.revalidate();
        questionCardPanel.repaint();
    }

    /**
     * 点击掌握按钮, 弹出掌握程度dialog
     * 在dialog中点击确认按钮, 则会同步刷新card数据, 并标记为已完成
     * 同时会向后台发出请求, 更新数据
     */
    private void showMasteryDialog() {
        new AbstractMasteryDialog(this, BundleUtils.i18nHelper("设置掌握程度", "set mastery level")) {

            @Override
            protected void setConfirmButtonListener(JButton confirmButton, ButtonGroup group) {
                confirmButton.addActionListener(e -> {
                    // 获取选中的掌握程度
                    String levelStr = group.getSelection().getActionCommand();

                    service.rateQuestion(FSRSRating.getById(levelStr));
                    // 更新问题状态
                    nextQuestion();
                    // 通知TotalReviewPlan刷新获取新的题目
                    env.post("refresh");

                    // 关闭对话框
                    this.dispose();
                });
            }
        };
    }

    /**
     * 下一题
     */
    private void nextQuestion() {
        if (this.questionCard != null) {
            questionCardPanel.remove(this.questionCard);
        }
        this.topQuestion = service.getTopQuestion();
        this.questionCard = createQuestionCard(topQuestion);
        // updatePageComp();
        questionCardPanel.add(this.questionCard, BorderLayout.CENTER);
        questionCardPanel.revalidate();
        questionCardPanel.repaint();
    }

    private void updateQuestionStatus(ReviewQuestion question, int masteryLevel) {
        // 更新问题状态
        service.rateTopQuestion(FSRSRating.values()[masteryLevel]);

        // 通知后台删除该题目
        service.deleteQuestion(question.getId());

        // 获取新的题目
        this.topQuestion = service.getTopQuestion();

        // 如果没有题目需要复习，更新UI显示完成信息
        if (topQuestion == null) {
            contentPanel.removeAll();
            this.currentCard = new JLabel(BundleUtils.i18nHelper("恭喜你，所有题目都已完成！", "Congratulations, all questions have been completed."));
            contentPanel.add(this.currentCard, BorderLayout.CENTER);
        } else {
            // 更新UI显示新题目信息
            if (currentCard instanceof JEditorPane) {
                ((JEditorPane) currentCard).setText(new CssBuilder()
                        .addStatus(topQuestion.getStatus())
                        .addTitle(topQuestion.getTitle(), question.getDifficulty())
                        .addUserRate(topQuestion.getUserRate())
                        .lastModify(topQuestion.getLastModify())
                        .nextReview(topQuestion.getNextReview())
                        .build());
            }
        }

        // 刷新UI
        contentPanel.revalidate();
        contentPanel.repaint();
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
                    service.updateBack(rq.getId(), newSolution);
                    System.out.println("Save solution: " + newSolution);
                }
            });
            textArea.setSize(globalSize);

            return textArea;
        }
    }
}
