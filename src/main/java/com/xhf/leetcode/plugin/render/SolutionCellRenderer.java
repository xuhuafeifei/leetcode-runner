package com.xhf.leetcode.plugin.render;

import com.xhf.leetcode.plugin.model.Solution;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SolutionCellRenderer<T> extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JTextArea textArea = new JTextArea();
        textArea.setText(value.toString());
        textArea.setLineWrap(false);
        textArea.setEditable(false);
        textArea.setForeground(new Color(92, 89, 89));
        textArea.setFont(list.getFont());

        textArea.setBorder(new LineBorder(new Color(224, 222, 222)));

        return textArea;
    }
}