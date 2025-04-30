package com.xhf.leetcode.plugin.review.utils;

import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

public abstract class AbstractMasteryDialog extends JDialog {

    public AbstractMasteryDialog(Component comp, String title) {
        super((Frame) SwingUtilities.getWindowAncestor(comp), title, true);

        setLayout(new BorderLayout());

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
            buttons[i].setActionCommand(String.valueOf(levels[i].getLevel()));
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

        setConfirmButtonListener(confirmButton, group);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(JBUI.Borders.empty(0, 10, 10, 10));
        buttonPanel.add(confirmButton);

        // 添加组件到对话框
        add(radioPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 设置对话框大小和位置
        pack();
        // 居中于屏幕
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected abstract void setConfirmButtonListener(JButton confirmButton, ButtonGroup group);
}
