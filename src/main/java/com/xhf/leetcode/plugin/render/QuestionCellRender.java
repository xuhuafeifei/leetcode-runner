package com.xhf.leetcode.plugin.render;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;

public class QuestionCellRender extends DefaultListCellRenderer {

    // 使用 JBColor 以适配深色/浅色主题
    private static final JBColor VIP_BORDER_COLOR = new JBColor(
        new Color(218, 165, 32),  // 浅色主题：金色
        new Color(205, 155, 29)   // 深色主题：深金色
    );

    private static final JBColor VIP_BACKGROUND_COLOR = new JBColor(
        new Color(255, 250, 205),  // 浅色主题：淡黄色
        new Color(50, 40, 0)       // 深色主题：深金色
    );

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
        String difficulty;
        String toStr;
        boolean isPaidOnly = false;

        if (value instanceof Question) {
            Question question = (Question) value;
            difficulty = question.getDifficulty();
            toStr = question.toString();
            isPaidOnly = question.getIsPaidOnly();
        } else if (value instanceof CompetitionQuestion) {
            // 兼容CompetitionQuestion
            CompetitionQuestion question = (CompetitionQuestion) value;
            difficulty = question.getDifficulty();
            toStr = question.toString();
        } else {
            // 兜底
            toStr = value.toString();
            int i = RandomUtils.nextInt(0, 2);
            // 这个别国际化, 这玩意儿是系统内部使用的变量, 不会在UI处向用户呈现
            String[] s = {"EASY", "MEDIUM", "HARD"};
            difficulty = s[i];
        }
        JLabel label = (JLabel) super.getListCellRendererComponent(list, toStr, index, isSelected, cellHasFocus);

        // 设置题目颜色
        switch (difficulty) {
            case "EASY":
                label.setForeground(Constants.GREEN_COLOR);
                break;
            case "MEDIUM":
                label.setForeground(Constants.YELLOW_COLOR);
                break;
            case "HARD":
                label.setForeground(Constants.RED_COLOR);
                break;
        }

        // 为 VIP 题目设置特殊样式
        if (isPaidOnly && !isSelected) {
            // 设置粗体字
            Font originalFont = label.getFont();
            label.setFont(originalFont.deriveFont(Font.BOLD));

            // 创建一个带有内边距和边框的复合边框
            Border innerBorder = BorderFactory.createLineBorder(VIP_BORDER_COLOR, 1);
            Border padding = BorderFactory.createEmptyBorder(1, 6, 1, 6);
            label.setBorder(BorderFactory.createCompoundBorder(padding, innerBorder));

            // 设置半透明背景色
            Color bgColor = VIP_BACKGROUND_COLOR;
            label.setBackground(new JBColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 40),
                new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 40)));
            label.setOpaque(true);
        } else {
            // 非 VIP 题目使用默认样式
            label.setBorder(JBUI.Borders.empty(2, 8));
            if (!isSelected) {
                label.setOpaque(false);
            }
        }

        return label;
    }
}