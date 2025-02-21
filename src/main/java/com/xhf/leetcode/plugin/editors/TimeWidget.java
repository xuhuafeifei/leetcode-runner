package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.xhf.leetcode.plugin.window.SearchPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 创建了一个悬浮窗口, 这玩意儿目前还没法主动关闭
 * 另外，时间也是固定死的, 不会改变. 我自己的一个初始想法是开一个线程
 * 每隔1s, 刷新一次label的时间
 */
public class TimeWidget {
    private final JWindow window;
    private final JBLabel label;
    // 记录鼠标按下时的偏移量
    private Point mouseDownOffset;

    public TimeWidget(@NotNull Project project) {
        this.window = new JWindow();
        this.label = new JBLabel("12:23");

        initWindow();
    }

    private void initWindow() {
        // 设置窗口内容
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(JBColor.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(JBColor.GRAY, 1)); // 添加边框
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel(IconLoader.getIcon("/icons/clock.svg", TimeWidget.class)), BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);

        // 设置窗口属性
        window.setContentPane(panel);
        window.setSize(200, 50); // 设置窗口大小
//        window.setAlwaysOnTop(true); // 始终置顶
//        window.setBackground(new Color(0, 0, 0, 0)); // 透明背景

        // 添加鼠标事件监听器以实现拖动功能
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 记录鼠标按下时的偏移量
                mouseDownOffset = e.getPoint();
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 计算窗口的新位置
                Point windowLocation = window.getLocation();
                int deltaX = e.getX() - mouseDownOffset.x;
                int deltaY = e.getY() - mouseDownOffset.y;
                window.setLocation(windowLocation.x + deltaX, windowLocation.y + deltaY);
            }
        });
    }

    public void show() {
        window.setVisible(true);
    }

    public void hide() {
        window.setVisible(false);
    }

    public void update(String text) {
        label.setText(text);
    }

    public void setLocation(int x, int y) {
        window.setLocation(x, y);
    }
}