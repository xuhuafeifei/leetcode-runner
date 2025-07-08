package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.xhf.leetcode.plugin.comp.MyJTextAreaWithPopupMenu;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * 负责显示代码 debug 过程中输出的 std out, std error 面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdPanel extends JPanel {

    private final StdOutPanel stdoutPanel;
    private final StdErrPanel stderrPanel;
    private final Application application = ApplicationManager.getApplication();

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
                // 标准输出
                stdoutPanel.setText(BundleUtils.i18n("action.leetcode.stdout") + "\n");
                // stderrPanel.setText("标准错误:\n");
                stderrPanel.setText(BundleUtils.i18n("action.leetcode.stderr") + "\n");
            });
        });
    }

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
    private static class StdOutPanel extends MyJTextAreaWithPopupMenu {

        public StdOutPanel() {
            setEditable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setText(BundleUtils.i18n("action.leetcode.stdout") + "\n");

            // 清空内容
            JMenuItem clearMenuItem = new JMenuItem(BundleUtils.i18n("action.leetcode.clear"));
            clearMenuItem.addActionListener(e -> {
                ApplicationManager.getApplication().invokeLater(() -> {
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        setText(BundleUtils.i18n("action.leetcode.stdout") + "\n");
                    });
                });
            });
            popupMenu.add(clearMenuItem);

            // 复制内容
            JMenuItem copyMenuItem = new JMenuItem(BundleUtils.i18n("copy"));
            copyMenuItem.addActionListener(e -> copy());
            popupMenu.add(copyMenuItem);
        }
    }

    // 用于显示 stderr 的面板，字体颜色与 IntelliJ 主题同步
    private static class StdErrPanel extends MyJTextAreaWithPopupMenu {

        public StdErrPanel() {
            setEditable(false);
            setText(BundleUtils.i18n("action.leetcode.stderr") + "\n");

            // 使用 JBColor 获取当前主题下的错误输出颜色
            JBColor errorColor = new JBColor(Color.RED, Color.RED); // 这里设置红色默认
            setForeground(errorColor);

            // 清空内容
            JMenuItem clearMenuItem = new JMenuItem(BundleUtils.i18n("action.leetcode.clear"));
            clearMenuItem.addActionListener(e -> {
                ApplicationManager.getApplication().invokeLater(() -> {
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        setText(BundleUtils.i18n("action.leetcode.stderr") + "\n");
                    });
                });
            });
            popupMenu.add(clearMenuItem);

            // 复制内容
            JMenuItem copyMenuItem = new JMenuItem(BundleUtils.i18n("copy"));
            copyMenuItem.addActionListener(e -> copy());
            popupMenu.add(copyMenuItem);
        }
    }

}
