package com.xhf.leetcode.plugin.setting;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.ReadType;

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
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(readType);
        add(outputType);
    }

    private JPanel createOutputType(String title, String[] options) {
        // 创建一个 Panel
        JBPanel<?> panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));

        // 创建 ButtonGroup 用于管理单选按钮
        this.outputType = new ButtonGroup();

        // 添加选项到 Panel 和 ButtonGroup
        for (String option : options) {
            JBRadioButton radioButton = new JBRadioButton(option);
            outputType.add(radioButton);
            panel.add(radioButton);
        }

        JPanel targetComponent = InnerHelpTooltip
                .FlowLayout(FlowLayout.LEFT)
                .add(panel)
                .addHelp("设置debug模式下, 调试内容显示位置。推荐使用UI显示。标准输出显示/console显示适合熟悉命令行的开发人员")
                .getTargetComponent();

        targetComponent.setBorder(new TitledBorder(title));
        return targetComponent;
    }

    public JPanel createReadType(String title, String[] options) {
        // 创建一个 Panel
        JBPanel<?> panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));

        // 创建 ButtonGroup 用于管理单选按钮
        this.readTypeGroup = new ButtonGroup();

        // 添加选项到 Panel 和 ButtonGroup
        for (String option : options) {
            JBRadioButton radioButton = new JBRadioButton(option);
            readTypeGroup.add(radioButton);
            panel.add(radioButton);
        }

        JPanel targetComponent = InnerHelpTooltip
                .FlowLayout(FlowLayout.LEFT)
                .add(panel)
                .addHelp("设置debug模式下, 指令输入来源。推荐使用UI指令读取。标准输入读取指令/命令行读取指令适合熟悉命令行的开发人员")
                .getTargetComponent();

        targetComponent.setBorder(new TitledBorder(title));
        return targetComponent;
    }

    public String getReadTypeName() {
        return getString(readTypeGroup);
    }

    public String getOutputTypeName() {
        return getString(outputType);
    }

    private String getString(ButtonGroup outputType) {
        if (outputType == null) return "";
        for (Enumeration<AbstractButton> buttons = outputType.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return "";
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
