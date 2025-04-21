package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.QueryDimModel;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.RQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.List;

/**
 * 每日一题面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DailyPlanTabPanel extends JPanel {
    private final Project project;
    private final ReviewQuestionService service;
    private List<ReviewQuestion> totalReviewQuestion;
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
    /**
     * 显示当前question card所在页数
     */
    private JLabel pageComp;

    // 掌握程度的枚举
    public enum MasteryLevel {
        AGAIN(
                FSRSRating.AGAIN.getName(),
                0,
                new JBColor(new Color(246, 11, 11), new Color(252, 67, 39)) // 浅色主题：鲜红；深色主题：亮红
        ),
        HARD(
                FSRSRating.HARD.getName(),
                1,
                new JBColor(new Color(255, 165, 0), new Color(255, 140, 0)) // 浅色主题：亮橙；深色主题：深橙
        ),
        GOOD(
                FSRSRating.GOOD.getName(),
                2,
                new JBColor(new Color(12, 163, 217), new Color(132, 195, 252)) // 浅色主题：亮蓝；深色主题：宝蓝
        ),
        EASY(
                FSRSRating.EASY.getName(),
                3,
                new JBColor(new Color(47, 183, 47), new Color(53, 166, 123)) // 浅色主题：翠绿；深色主题：亮绿
        );

        private final String description; // 描述信息
        private final int level;          // 等级
        private final JBColor color;      // 颜色（支持浅色和深色主题）

        MasteryLevel(String description, int level, JBColor color) {
            this.description = description;
            this.level = level;
            this.color = color;
        }

        @Override
        public String toString() {
            return description;
        }

        public JBColor getColor() {
            return color;
        }

        public int getLevel() {
            return this.level;
        }
    }

    public DailyPlanTabPanel(Project project) {
        this.project = project;
        this.service = new RQServiceImpl(project);
        loadContent();
    }

    int cursor = -1;

    private void loadContent() {
        this.totalReviewQuestion = service.getAllQuestions();
                //service.getTotalReviewQuestion(new QueryDimModel());
        this.questionCardPanel = new JPanel();
        questionCardPanel.setLayout(new BorderLayout());

        if (! totalReviewQuestion.isEmpty()) {
            this.questionCard = createQuestionCard(totalReviewQuestion.get(++cursor));
            questionCardPanel.add(this.questionCard, BorderLayout.CENTER);
        }

        var lef = new JButton(IconLoader.getIcon("/icons/left-btn.svg", this.getClass()));
        lef.setBorder(BorderFactory.createEmptyBorder());
        lef.setContentAreaFilled(false);
        lef.addActionListener(e -> {
            if (cursor > 0) {
                if (this.questionCard != null) {
                    questionCardPanel.remove(this.questionCard);
                }
                this.questionCard = createQuestionCard(totalReviewQuestion.get(--cursor));
                updatePageComp();
                questionCardPanel.add(this.questionCard, BorderLayout.CENTER);
                questionCardPanel.revalidate();
                questionCardPanel.repaint();
            }
        });

        var rig = new JButton(IconLoader.getIcon("/icons/right-btn.svg", this.getClass()));
        rig.setBorder(BorderFactory.createEmptyBorder());
        rig.setContentAreaFilled(false);
        rig.addActionListener(e -> {
            if (cursor < totalReviewQuestion.size() - 1) {
                if (this.questionCard != null) {
                    questionCardPanel.remove(this.questionCard);
                }
                this.questionCard = createQuestionCard(totalReviewQuestion.get(++cursor));
                updatePageComp();
                questionCardPanel.add(this.questionCard, BorderLayout.CENTER);
                questionCardPanel.revalidate();
                questionCardPanel.repaint();
            }
        });

        questionCardPanel.add(lef, BorderLayout.WEST);
        questionCardPanel.add(rig, BorderLayout.EAST);

        add(questionCardPanel);
    }

    private Dimension globalSize;

    /**
     * 更新pageComp
     */
    private void updatePageComp() {
        this.pageComp.setText(cursor + 1 + " / " + totalReviewQuestion.size());
    }

    private Component createQuestionCard(ReviewQuestion rq) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        this.pageComp = new JLabel();
        this.pageComp.setFont(Constants.ENGLISH_FONT);
        this.pageComp.setHorizontalAlignment(SwingConstants.CENTER);
        this.pageComp.setBorder(JBUI.Borders.empty(3, 0));
        updatePageComp();

        jPanel.add(this.pageComp, BorderLayout.NORTH);

        // 创建内容面板容器
        contentPanel = new JPanel(new BorderLayout());
        this.currentCard = createEditorComponent(rq);  // 保存引用
        contentPanel.add(this.currentCard, BorderLayout.CENTER);
        jPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        // 掌握button
        JButton masterBtn = new JButton(BundleUtils.i18nHelper("掌握", "master"));
        masterBtn.addActionListener(e -> showMasteryDialog(rq));

        // 继续学习button
        JButton deleteBtn = new JButton(BundleUtils.i18nHelper("删除", "delete"));
        deleteBtn.addActionListener(e -> removeQuestion(rq));
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
        bottomPanel.add(deleteBtn);
        bottomPanel.add(flipButton);

        jPanel.add(bottomPanel, BorderLayout.SOUTH);

        return jPanel;
    }

    private void removeQuestion(ReviewQuestion rq) {
        // todo: 调用实际的后端服务删除题目

        // 列表删除
        if (cursor == totalReviewQuestion.size() - 1) {
            // 最后一个题目, 游标指向前一个
            cursor--;
        }
        totalReviewQuestion.remove(rq);
        updatePageComp();
        // 刷新UI
        this.questionCardPanel.remove(this.questionCard);
        if (cursor != -1) {
            this.questionCard = createQuestionCard(totalReviewQuestion.get(cursor));
            questionCardPanel.add(this.questionCard, BorderLayout.CENTER);
        }
        questionCardPanel.revalidate();
        questionCardPanel.repaint();
    }

    /**
     * 点击掌握按钮, 弹出掌握程度dialog
     * 在dialog中点击确认按钮, 则会同步刷新card数据, 并标记为已完成
     * 同时会向后台发出请求, 更新数据
     * @param question question
     */
    private void showMasteryDialog(ReviewQuestion question) {
        // 创建对话框
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), BundleUtils.i18nHelper("设置掌握程度", "set mastery level"), true);
        dialog.setLayout(new BorderLayout());

        // 创建单选按钮面板
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.setBorder(JBUI.Borders.empty(10));

        // 创建按钮组
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[MasteryLevel.values().length];

        // 创建单选按钮
        MasteryLevel[] levels = MasteryLevel.values();
        for (int i = 0; i < levels.length; i++) {
            buttons[i] = new JRadioButton(levels[i].toString());
            buttons[i].setActionCommand(String.valueOf(levels[i].level));
            buttons[i].setForeground(levels[i].getColor());  // 设置文字颜色
            buttons[i].setFont(Constants.CN_FONT_BOLD);

            // 创建带颜色边框的面板
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(levels[i].getColor(), 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            buttonPanel.setOpaque(false);
            buttonPanel.add(buttons[i], BorderLayout.CENTER);

            group.add(buttons[i]);
            radioPanel.add(buttonPanel);

            // 添加一些垂直间距
            if (i < levels.length - 1) {
                radioPanel.add(Box.createVerticalStrut(8));
            }
        }

        // 默认选中"基本掌握"
        buttons[1].setSelected(true);

        // 创建确认按钮
        JButton confirmButton = new JButton(BundleUtils.i18nHelper("确认", "confirm"));
        confirmButton.addActionListener(e -> {
            // 获取选中的掌握程度
            String levelStr = group.getSelection().getActionCommand();
            MasteryLevel level = MasteryLevel.valueOf(levelStr);

            // 更新问题状态
            updateQuestionStatus(question, level.getLevel());

            // 关闭对话框
            dialog.dispose();
        });

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(JBUI.Borders.empty(0, 10, 10, 10));
        buttonPanel.add(confirmButton);

        // 添加组件到对话框
        dialog.add(radioPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 设置对话框大小和位置
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateQuestionStatus(ReviewQuestion question, int masteryLevel) {
        // 更新问题状态
        service.rateTopQuestion(FSRSRating.values()[masteryLevel]);

        // 通知后台删除该题目
        service.deleteQuestion(question.getId());

        // 获取新的题目
        ReviewQuestion topQuestion = service.getTopQuestion();

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
                    // TODO: 在这里实现保存逻辑
                    System.out.println("Save solution: " + newSolution);
                }
            });
            textArea.setSize(globalSize);

            return textArea;
        }
    }
}
