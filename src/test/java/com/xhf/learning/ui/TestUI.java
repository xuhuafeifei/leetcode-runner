package com.xhf.learning.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendabletextArea;
import org.junit.Test;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class TestUI {
    @Test
    public void testButton() {
        // 创建并配置 JFrame
        JFrame frame = new JFrame("Swing Application");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // 添加一个标签
        JLabel label = new JLabel("Hello, Swing!", SwingConstants.CENTER);
        frame.getContentPane().add(label);

        ExtendableTextComponent.Extension browseExtension =
                ExtendableTextComponent.Extension.create(
                        AllIcons.General.OpenDisk,
                        AllIcons.General.OpenDiskHover,
                        "Open file",
                        () -> System.out.println("Browse file clicked")
                );
        ComboBox<String> extComboBox = new ComboBox();
        extComboBox.setEditable(true);
        extComboBox.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JtextArea createEditorComponent() {
                ExtendabletextArea ecbEditor = new ExtendabletextArea();
                ecbEditor.addExtension(browseExtension);
                ecbEditor.setBorder(null);
                return ecbEditor;
            }
        });

        frame.add(extComboBox);

        // 显示窗口
        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        while (true) {}
    }
}
