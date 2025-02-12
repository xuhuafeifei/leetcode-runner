package com.xhf.leetcode.plugin.render;

import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.RandomUtils;

import javax.swing.*;
import java.awt.*;

public class QuestionCellRender extends DefaultListCellRenderer {


    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String difficulty;
        String toStr;
        if (value instanceof Question) {
            Question question = (Question) value;
            difficulty = question.getDifficulty();
            toStr = question.toString();
        } else if (value instanceof CompetitionQuestion) {
            // 兼容CompetitionQuestion
            CompetitionQuestion question = (CompetitionQuestion) value;
            difficulty = question.getDifficulty();
            toStr = question.toString();
        } else {
            // 兜底
            toStr = value.toString();
            int i = RandomUtils.nextInt(0, 2);
            String[] s = {"EASY", "MEDIUM", "HARD"};
            difficulty = s[i];
        }
        JLabel label = (JLabel) super.getListCellRendererComponent(list, toStr, index, isSelected, cellHasFocus);
        switch (difficulty) {
            case "EASY": label.setForeground(Constants.GREEN_COLOR); break;
            case "MEDIUM": label.setForeground(Constants.YELLOW_COLOR); break;
            case "HARD": label.setForeground(Constants.RED_COLOR); break;
        }
        return label;
    }
}