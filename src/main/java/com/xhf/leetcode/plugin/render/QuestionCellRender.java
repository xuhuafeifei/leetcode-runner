package com.xhf.leetcode.plugin.render;

import com.xhf.leetcode.plugin.model.Question;

import javax.swing.*;
import java.awt.*;

public class QuestionCellRender extends DefaultListCellRenderer {
    private static Color Level1 = new Color(92, 184, 92);
    private static Color Level2 = new Color(240, 173, 78);
    private static Color Level3 = new Color(217, 83, 79);

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Question question = (Question) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, question.toString(), index, isSelected, cellHasFocus);
        switch (question.getDifficulty()) {
            case "EASY": label.setForeground(Level1); break;
            case "MEDIUM": label.setForeground(Level2); break;
            case "HARD": label.setForeground(Level3); break;
        }
        return label;
    }
}