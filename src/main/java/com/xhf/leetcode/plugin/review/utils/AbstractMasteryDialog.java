package com.xhf.leetcode.plugin.review.utils;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public abstract class AbstractMasteryDialog extends JDialog {

    public AbstractMasteryDialog(Component comp, String title) {
        super((Frame) SwingUtilities.getWindowAncestor(comp), title, true);
        initUI();
    }

    private void initUI() {
        // 获取当前主题颜色
        Color bgColor = UIManager.getColor("Panel.background");
        Color fgColor = UIManager.getColor("Label.foreground");
        Color borderColor = UIManager.getColor("Component.borderColor");
        Color selectionBg = UIManager.getColor("Button.select");

        // 设置对话框背景
        getContentPane().setBackground(bgColor);

        // 使用GridBagLayout实现精确对齐
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10, 15));
        contentPanel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 备注标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel(BundleUtils.i18nHelper("备注", "Notes") + ":");
        noteLabel.setFont(Constants.CN_FONT_BOLD);
        noteLabel.setForeground(fgColor);
        contentPanel.add(noteLabel, gbc);

        // 2. 备注文本框
        gbc.gridy++;
        gbc.weightx = 1.0;
        JTextArea noteTextArea = new JTextArea(3, 25);
        noteTextArea.setLineWrap(true);
        noteTextArea.setWrapStyleWord(true);
        noteTextArea.setFont(Constants.CN_FONT);
        noteTextArea.setBackground(UIManager.getColor("TextArea.background"));
        noteTextArea.setForeground(fgColor);
        JScrollPane noteScrollPane = new JScrollPane(noteTextArea);
        noteScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        contentPanel.add(noteScrollPane, gbc);

        // 3. 掌握程度标签
        gbc.gridy++;
        gbc.weightx = 0;
        JLabel masteryLabel = new JLabel(BundleUtils.i18nHelper("掌握程度", "Mastery Level"));
        masteryLabel.setFont(Constants.CN_FONT_BOLD);
        masteryLabel.setForeground(fgColor);
        contentPanel.add(masteryLabel, gbc);

        // 4. 单选按钮面板
        gbc.gridy++;
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.setBorder(JBUI.Borders.empty(5, 10));
        radioPanel.setBackground(bgColor);

        ButtonGroup group = new ButtonGroup();
        MasteryLevel[] levels = MasteryLevel.values();
        for (int i = 0; i < levels.length; i++) {
            JRadioButton button = createAlignedRadioButton(levels[i], bgColor);
            if (i == 1) {
                // 默认选中"有点困难"
                button.setSelected(true);
            }
            group.add(button);
            radioPanel.add(button);
            if (i < levels.length - 1) {
                radioPanel.add(Box.createVerticalStrut(8));
            }
        }
        contentPanel.add(radioPanel, gbc);

        // 5. 确认按钮
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton confirmButton = new JButton(BundleUtils.i18nHelper("确认", "Confirm"));
        confirmButton.setFont(Constants.CN_FONT_BOLD);
        confirmButton.setBackground(selectionBg != null ? selectionBg : new Color(70, 130, 180));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        setConfirmButtonListener(confirmButton, group, noteTextArea);
        contentPanel.add(confirmButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // 设置对话框大小和位置
        pack();
        // 居中于屏幕
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JRadioButton createAlignedRadioButton(MasteryLevel level, Color bgColor) {
        JRadioButton button = new JRadioButton(level.toString());
        button.setActionCommand(String.valueOf(level.getLevel()));
        button.setForeground(level.getColor());
        button.setFont(Constants.CN_FONT);
        button.setBackground(bgColor);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(level.getColor(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 15)
        ));
        return button;
    }

    protected abstract void setConfirmButtonListener(JButton confirmButton, ButtonGroup group, JTextArea noteTextArea);
}