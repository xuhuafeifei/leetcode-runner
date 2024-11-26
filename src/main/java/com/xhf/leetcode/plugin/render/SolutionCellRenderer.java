package com.xhf.leetcode.plugin.render;

import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.model.Submission;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SolutionCellRenderer<T> extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Solution solution = (Solution) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, solution.toString(), index, isSelected, cellHasFocus);
        if (index < 6) {
            label.setForeground(new Color(239, 61, 61));
        } else if (index < 30) {
            label.setForeground(new Color(243, 134, 24));
        } else {
            label.setForeground(new Color(131, 129, 129));
        }
        return label;
    }
}