package com.xhf.leetcode.plugin.render;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.Constants;

import javax.swing.*;
import java.awt.*;

public class VariablesCellRender extends DefaultListCellRenderer {


    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String exp = (String) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, exp, index, isSelected, cellHasFocus);
        // 为label设置斜体加粗
        // 是斜体 + 粗体
        if (exp.contains(Constants.STATIC_VARIABLE) ||
                exp.contains(Constants.MEMBER_VARIABLE) ||
                exp.contains(Constants.LOCAL_VARIABLE))
        {
            label.setFont(new Font(Constants.ENGLISH_FONT.getFontName(), Font.ITALIC + Font.BOLD, label.getFont().getSize()));
        }
        return label;
    }
}