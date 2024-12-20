package com.xhf.leetcode.plugin.setting;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.utils.Constants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Enumeration;

public class DebugPanel extends JPanel{

    private ButtonGroup readTypeGroup;
    private ButtonGroup outputType;

    public DebugPanel() {
        JPanel readType = createReadType("read type", ReadType.getNames());
        JPanel outputType = createOutputType("output type", OutputType.getNames());
        add(readType);
        add(outputType);
    }

    private JPanel createOutputType(String title, String[] options) {
        // 创建一个 Panel
        JBPanel<?> panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder(title)); // 给 Panel 设置边框和标题
        panel.setBackground(Constants.BACKGROUND_COLOR);

        // 创建 ButtonGroup 用于管理单选按钮
        this.outputType = new ButtonGroup();

        // 添加选项到 Panel 和 ButtonGroup
        for (String option : options) {
            JBRadioButton radioButton = new JBRadioButton(option);
            outputType.add(radioButton);
            panel.add(radioButton);
            // 默认控制台
            if (option.equals(OutputType.CONSOLE_OUT.getName())) {
                radioButton.setSelected(true);
            }
        }

        return panel;
    }

    public JPanel createReadType(String title, String[] options) {
        // 创建一个 Panel
        JBPanel<?> panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder(title)); // 给 Panel 设置边框和标题
        panel.setBackground(Constants.BACKGROUND_COLOR);

        // 创建 ButtonGroup 用于管理单选按钮
        this.readTypeGroup = new ButtonGroup();

        // 添加选项到 Panel 和 ButtonGroup
        for (String option : options) {
            JBRadioButton radioButton = new JBRadioButton(option);
            readTypeGroup.add(radioButton);
            panel.add(radioButton);
            // 默认命令行
            if (option.equals(ReadType.COMMAND_IN.getName())) {
                radioButton.setSelected(true);
            }
        }

        return panel;
    }

    public String getReadTypeName() {
        for (Enumeration<AbstractButton> buttons = readTypeGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    public String getOutputTypeName() {
        for (Enumeration<AbstractButton> buttons = outputType.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    public void setOutputTypeName(String outputTypeName) {
        // 已选择的取消设置
        this.outputType.setSelected(this.outputType.getSelection(), false);
        // 选择
        for (Enumeration<AbstractButton> buttons = this.outputType.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.getText().equals(outputTypeName)) {
                button.setSelected(true);
                break;
            }
        }
    }

    public void setReadTypeName(String readTypeName) {
        // 已选择的取消设置
        this.readTypeGroup.setSelected(this.readTypeGroup.getSelection(), false);
        // 选择
        for (Enumeration<AbstractButton> buttons = this.readTypeGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.getText().equals(readTypeName)) {
                button.setSelected(true);
                break;
            }
        }
    }
}
