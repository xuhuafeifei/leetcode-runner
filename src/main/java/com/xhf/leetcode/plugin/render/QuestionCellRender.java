package com.xhf.leetcode.plugin.render;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.Constants;

import javax.swing.*;
import java.awt.*;

public class QuestionCellRender extends DefaultListCellRenderer {


    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Question question = (Question) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, question.toString(), index, isSelected, cellHasFocus);
        switch (question.getDifficulty()) {
            case "EASY": label.setForeground(Constants.GREEN_COLOR); break;
            case "MEDIUM": label.setForeground(Constants.YELLOW_COLOR); break;
            case "HARD": label.setForeground(Constants.RED_COLOR); break;
        }
        return label;
    }
}