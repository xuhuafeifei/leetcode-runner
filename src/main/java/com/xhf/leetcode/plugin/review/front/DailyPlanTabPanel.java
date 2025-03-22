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
import com.xhf.leetcode.plugin.utils.Constants;

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

    // 掌握程度的枚举
    public enum MasteryLevel {
        PERFECT(
                "完全掌握【9-10】", // 描述信息
                5,
                new JBColor(new Color(246, 11, 11), new Color(252, 67, 39)) // 浅色主题：鲜红；深色主题：亮红
        ),
        GOOD(
                "基本掌握【7-8】",
                4,
                new JBColor(new Color(255, 165, 0), new Color(255, 140, 0)) // 浅色主题：亮橙；深色主题：深橙
        ),
        MODERATE(
                "一般理解【5-6】",
                3,
                new JBColor(new Color(222, 186, 27), new Color(225, 171, 9)) // 浅色主题：亮黄；深色主题：琥珀黄
        ),
        BASIC(
                "初步了解【3-4】",
                2,
                new JBColor(new Color(47, 183, 47), new Color(53, 166, 123)) // 浅色主题：翠绿；深色主题：亮绿
        ),
        POOR(
                "需要加强【1-2】",
                1,
                new JBColor(new Color(12, 163, 217), new Color(26, 122, 217)) // 浅色主题：亮蓝；深色主题：宝蓝
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

        public static MasteryLevel fromScore(int score) {
            if (score >= 9 && score <= 10) {
                return PERFECT;
            } else if (score >= 7 && score <= 8) {
                return GOOD;
            } else if (score >= 5 && score <= 6) {
                return MODERATE;
            } else if (score >= 3 && score <= 4) {
                return BASIC;
            } else if (score >= 1 && score <= 2) {
                return POOR;
            } else {
                throw new IllegalArgumentException("分数应在1到10之间");
            }
        }
    }

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
        masterBtn.addActionListener(e -> showMasteryDialog(rq));
        
        // 继续学习button
        JButton deleteBtn = new JButton(BundleUtils.i18nHelper("删除", "delete"));
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
            int masteryLevel = Integer.parseInt(levelStr);
            
            // 更新问题状态
            updateQuestionStatus(question, masteryLevel);
            
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
        // 模拟发送请求给后台
        System.out.println("Updating question status: questionId=" + question.getTitle() + 
                          ", masteryLevel=" + masteryLevel);
        
        // TODO: 调用实际的后端服务更新状态
        // 模拟更新UI
        if (currentCard instanceof JEditorPane) {
            ((JEditorPane) currentCard).setText(
                new CssBuilder()
                    .addStatus(ReviewStatus.DONE.getName())  // 更新状态为已完成
                    .addTitle(question.getTitle(), question.getDifficulty())
                    .addUserRate(question.getUserRate())
                    .lastModify(question.getLastModify())
                    .nextReview(question.getNextReview())
                    .build()
            );
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
