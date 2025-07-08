package com.xhf.leetcode.plugin.render;

import com.intellij.ui.JBColor;
import com.xhf.leetcode.plugin.model.Submission;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmissionCellRender extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
        Submission submission = (Submission) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, submission.toString(), index, isSelected,
            cellHasFocus);
        if (submission.isAc()) {
            label.setForeground(new JBColor(new Color(33, 143, 33), new Color(149, 255, 123)));
        } else {
            label.setForeground(JBColor.RED);
        }
        return label;
    }
}
