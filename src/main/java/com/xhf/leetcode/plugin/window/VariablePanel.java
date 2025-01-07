package com.xhf.leetcode.plugin.window;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.listener.PopupMenuAdaptor;
import com.xhf.leetcode.plugin.render.VariablesCellRender;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class VariablePanel extends JPanel {
    /**
     * 存储变量列表
     */
    private final MyList<String> variableList;
    /**
     * 输入表达式的文本框
     */
    private final JTextField expressionField;
    private final String defaultText = "Evaluate expression (Enter) or add a watch";

    public VariablePanel() {
        // 设置垂直布局
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 创建表达式输入区域（包含输入框和按钮）
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        // 输入框
        this.expressionField = new JTextField(defaultText);
        this.expressionField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // 如果当前文字是默认文字，清空内容并设置文字颜色为黑色
                if (expressionField.getText().equals(defaultText)) {
                    expressionField.setText("");
                    expressionField.setForeground(JBColor.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 如果用户未输入内容，恢复默认文字并设置颜色为灰色
                if (expressionField.getText().isEmpty()) {
                    expressionField.setText(defaultText);
                    expressionField.setForeground(JBColor.GRAY);
                }
            }
        });
        // 监听回车事件
        this.expressionField.addActionListener(e -> {
            // 发送P指令
            String text = expressionField.getText();
            if (defaultText.equals(text)) {
                return;
            }
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, text));
        });
        inputPanel.add(expressionField);


        // 按钮（右侧图标按钮）
        JButton watchButton = new JButton(AllIcons.Debugger.AddToWatch);
        inputPanel.add(watchButton);
        watchButton.addActionListener(e -> {
            String exp = expressionField.getText();
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.WATCH, exp));
        });

        // 添加表达式输入区域到主面板
        add(inputPanel);

        variableList = new MyList<>();
        // 变量列表
        variableList.setCellRenderer(new VariablesCellRender());
        // 添加menu
        variableList.addMouseListener(new PopupMenuAdaptor<>(variableList));

        // 添加滚动面板
        JBScrollPane scrollPane = new JBScrollPane(variableList);
        add(scrollPane);
    }

    /**
     * 获取变量列表
     *
     * @return 变量列表
     */
    public MyList<String> getVariables() {
        return variableList;
    }
}

