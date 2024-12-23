package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * 负责显示代码 debug 过程中输出的 std out, std error 面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdPanel extends JPanel {

    private final StdOutPanel stdoutPanel;
    private final StdErrPanel stderrPanel;

    public StdPanel() {
        // 创建一个 JBSplitter，支持垂直分割，第一块用于显示 stdout，第二块用于显示 stderr
        JBSplitter splitter = new JBSplitter(true, 0.5f);
        splitter.setSplitterProportionKey("StdPanel.SplitterProportion");

        // 设置第一块为 stdout 面板
        this.stdoutPanel = new StdOutPanel();
        splitter.setFirstComponent(new JScrollPane(stdoutPanel));

        // 设置第二块为 stderr 面板
        this.stderrPanel = new StdErrPanel();
        splitter.setSecondComponent(new JScrollPane(stderrPanel));

        // 添加 JBSplitter 到 StdPanel
        setLayout(new BorderLayout());
        add(splitter, BorderLayout.CENTER);
    }

    public void clear() {
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                stdoutPanel.setText("Standard Output:\n");
                stderrPanel.setText("Standard Error:\n");
            });
        });
    }

    private final Application application = ApplicationManager.getApplication();

    public void setStdoutContent(String stdout) {
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                stdoutPanel.setText(stdout);
            });
        });
    }

    public void setStderrContent(String stderr) {
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                stderrPanel.setText(stderr);
            });
        });
    }

    public void appendStdoutContent(String result) {
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                String text = stdoutPanel.getText();
                stdoutPanel.setText(text + result);
            });
        });
    }

    public void appendStderrContent(String result) {
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                String text = stderrPanel.getText();
                stderrPanel.setText(text + result);
            });
        });
    }

    // 用于显示 stdout 的面板
    private static class StdOutPanel extends JTextArea {
        public StdOutPanel() {
            setEditable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setText("Standard Output:\n");
        }
    }

    // 用于显示 stderr 的面板，字体颜色与 IntelliJ 主题同步
    private static class StdErrPanel extends JTextPane {

        public StdErrPanel() {
            setEditable(false);
            setText("Standard Error:\n");

            // 使用 JBColor 获取当前主题下的错误输出颜色
            JBColor errorColor = new JBColor(Color.RED, Color.RED); // 这里设置红色默认
            setForeground(errorColor);

            // 设置样式
            StyledDocument doc = getStyledDocument();
            Style style = doc.addStyle("ErrorStyle", null);
            StyleConstants.setForeground(style, errorColor);

//            // 添加初始文本为红色
//            try {
//                doc.insertString(doc.getLength(), "", style);
//            } catch (BadLocationException e) {
//                e.printStackTrace();
//            }
        }
    }
}
