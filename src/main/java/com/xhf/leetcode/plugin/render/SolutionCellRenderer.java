package com.xhf.leetcode.plugin.render;

import com.intellij.ui.JBColor;
import com.xhf.leetcode.plugin.model.Solution;

import javax.swing.*;
import java.awt.*;

public class SolutionCellRenderer<T> extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Solution solution = (Solution) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, solution.toString(), index, isSelected, cellHasFocus);
        if (index < 6) {
            label.setForeground(new JBColor(new Color(239, 61, 61), new Color(239, 61, 61)));
        } else if (index < 30) {
            label.setForeground(new JBColor(new Color(243, 134, 24), new Color(243, 134, 24)));
        } else {
            label.setForeground(new JBColor(new Color(131, 129, 129), new Color(241, 235, 233)));
        }
        return label;
    }
}