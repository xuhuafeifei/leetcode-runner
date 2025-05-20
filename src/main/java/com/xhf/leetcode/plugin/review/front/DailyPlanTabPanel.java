package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.RQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.review.utils.AbstractMasteryDialog;
import com.xhf.leetcode.plugin.review.utils.MessageReceiveInterface;
import com.xhf.leetcode.plugin.review.utils.ReviewConstants;
import com.xhf.leetcode.plugin.review.utils.ReviewUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 每日一题面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DailyPlanTabPanel extends JPanel implements MessageReceiveInterface {
    private final Project project;

    private final ReviewQuestionService service;

    private final ReviewEnv env;

    private boolean isShowingSolution = true;

    /**
     * 卡片面板, 包含内容+底部button
     */
    private JPanel questionCard;

    /**
     * 当前面板, 只包含卡片内容信息
     */
    private Component currentCard;

    private Dimension globalSize;

    private @Nullable ReviewQuestion topQuestion;

    // 底部按钮panel
    private JPanel bottomPanel;

    public DailyPlanTabPanel(Project project, ReviewEnv env) {
        this.project = project;
        this.service = new RQServiceImpl(project);
        this.env = env;
        this.env.registerListener(this);
        loadContent();
    }

    @Override
    public void onMessageReceived(String msg) {
        if (ReviewConstants.GET_TOP_QUESTION.equals(msg)) {
            nextQuestion();
        }
    }

    private void loadContent() {
        setLayout(new BorderLayout());

        this.topQuestion = service.getTopQuestion();
        this.questionCard = createQuestionCard(topQuestion);

        var iconBtn = new IconButton(BundleUtils.i18nHelper("翻转卡片", "flip card"), IconLoader.getIcon("/icons/flip.svg", DailyPlanTabPanel.class));
        var flipBtn = new JButton(iconBtn);
        flipBtn.setBorder(JBUI.Borders.empty(0, 5));
        flipBtn.addActionListener(doFlip(flipBtn, topQuestion));

        add(flipBtn, BorderLayout.NORTH);
        add(this.questionCard, BorderLayout.CENTER);
    }


    private JPanel createQuestionCard(ReviewQuestion rq) {
        // 创建内容面板容器
        JPanel jPanel = new JPanel(new BorderLayout());
        // 不论是solution还是empty, 都需要先把底层的按钮panel移除
        if (bottomPanel != null) {
            remove(bottomPanel);
        }

        if (rq != null) {
            this.currentCard = createEditorComponent(rq);
            jPanel.add(this.currentCard, BorderLayout.CENTER);
            if (isShowingSolution) {
                // 只有显示题解状态才能够显示底层按钮
                this.bottomPanel = getjPanel(topQuestion);
                // 底部按钮容器直接添加当this
                add(bottomPanel, BorderLayout.SOUTH);
            }
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
        masterBtn.addActionListener(e -> showMasteryDialog(rq));

        // 继续学习button
        JButton deleteBtn = new JButton(BundleUtils.i18nHelper("删除", "delete"));
        deleteBtn.addActionListener(e -> removeQuestion(rq));

        // 解题button
        JButton doItBtn = new JButton(BundleUtils.i18nHelper("做题", "do it"));
        doItBtn.addActionListener(e -> ReviewUtils.doIt(rq, project, env));

        // 反转button
        // JButton flipButton = getFlipBtn(rq);

        bottomPanel.add(masterBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(doItBtn);
        return bottomPanel;
    }

    private String createHtmlContent() {
        return "<html>" +
            "<head><style>" +
            "body { font-family: Arial; font-size: 16px; color: #666; } " +
            ".empty-message { text-align: center; padding: 20px; margin: 20px; }" +
            ".icon { font-size: 32px; color: gray; }" +
            "</style></head>" +
            "<body>" +
            "<div class=\"empty-message\">" +
            "    <div class=\"icon\">☑\uFE0F</div>" +
            "    <div>${message}</div>" +
            "</div>" +
            "</body></html>";
    }

    private ActionListener doFlip(JButton button, ReviewQuestion rq) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取当前Question面板的大小
                if (isShowingSolution) {
                    // isShowingSolution为true, 表示此时面板是显示题解, 需要记录此时的大小
                    globalSize = currentCard.getSize();
                }

                isShowingSolution = !isShowingSolution;
                // 更新按钮文字
                button.setText(BundleUtils.i18nHelper(
                    isShowingSolution ? "查看题目" : "查看题解",
                    isShowingSolution ? "question" : "solution"
                ));

                // 替换编辑器组件
                remove(questionCard);
                questionCard = createQuestionCard(rq);
                add(questionCard, BorderLayout.CENTER);
            }
        };
    }

    private void removeQuestion(ReviewQuestion rq) {
        service.deleteQuestion(rq.getId());

        // 刷新UI
        this.remove(this.questionCard);
        this.topQuestion = service.getTopQuestion();
        this.questionCard = createQuestionCard(this.topQuestion);

        add(this.questionCard, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * 点击掌握按钮, 弹出掌握程度dialog
     * 在dialog中点击确认按钮, 则会同步刷新card数据, 并标记为已完成
     * 同时会向后台发出请求, 更新数据
     */
    private void showMasteryDialog(ReviewQuestion rq) {
        new AbstractMasteryDialog(this, BundleUtils.i18nHelper("设置掌握程度", "set mastery level")) {

            @Override
            protected String getNoteText() {
                return rq.getUserNoteText();
            }

            @Override
            protected void setConfirmButtonListener(JButton confirmButton, ButtonGroup group, JTextArea textArea) {
                confirmButton.addActionListener(e -> {
                    // 获取选中的掌握程度
                    String levelStr = group.getSelection().getActionCommand();

                    service.rateQuestion(FSRSRating.getById(levelStr), textArea.getText());
                    // 更新DailyPlanTabPanel的问题状态
                    env.post(ReviewConstants.GET_TOP_QUESTION);
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
            remove(this.questionCard);
        }
        this.topQuestion = service.getTopQuestion();
        this.questionCard = createQuestionCard(topQuestion);
        // updatePageComp();
        add(this.questionCard, BorderLayout.CENTER);
        revalidate();
        repaint();
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
            textArea.setText(rq.getUserNoteText());
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
                    // 更新ReviewQuestion
                    rq.setBack(newSolution);
                    // 通知TotalReviewPlan刷新获取新的题目
                    env.post(ReviewConstants.REFRESH);
                    LogUtils.simpleDebug("Save solution: " + newSolution);
                }
            });
            textArea.setSize(globalSize);

            return textArea;
        }
    }
}
