package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author 文艺倾年
 * @email 1531137510@qq.com
 */
public class TimerPanel {

    private final JPanel contentPanel;
    private final JBLabel label;

    public JComponent getContentPanel() {
        return contentPanel;
    }

    // 计时器相关变量
    private final Timer timer;
    private long startTime = 0;
    private long pausedTime = 0;
    private boolean isRunning = false;
    private JButton toggleButton;

    public TimerPanel(@NotNull Project project) {
        this.label = new JBLabel("00:00:00");
        this.contentPanel = new JPanel(new BorderLayout());

        // 初始化计时器（每50ms更新一次）
        timer = new Timer(50, this::updateTime);

        initComponents();
    }

    private void initComponents() {

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(JBColor.background());
        mainPanel.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));

        // 左侧图标
        JLabel iconLabel = new JLabel(IconLoader.getIcon("/icons/clock.svg", TimerPanel.class));

        // 控制按钮面板
        JPanel controlPanel = createControlPanel();

        // 组装组件
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(label, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        contentPanel.add(mainPanel);
    }

    // 控制按钮面板
    private JPanel createControlPanel() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        panel.setOpaque(false);

        // 使用成员变量保持按钮强引用，避免GC过早回收
        toggleButton = createIconButton("/icons/start.png", this::toggleTimer);
        JButton resetButton = createIconButton("/icons/reset.png", this::resetTimer);

        panel.add(toggleButton);
        panel.add(resetButton);
        return panel;
    }

    private JButton createIconButton(String iconPath, ActionListener listener) {
        JButton button = new JButton();
        button.setIcon(IconLoader.getIcon(iconPath, TimerPanel.class));
        button.addActionListener(listener);
        button.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        button.setContentAreaFilled(false);
        return button;
    }

    private void toggleTimer(ActionEvent e) {
        if (!isRunning) {
            startTimer();
            toggleButton.setIcon(IconLoader.getIcon("/icons/pause.png", TimerPanel.class));
        } else {
            pauseTimer();
            toggleButton.setIcon(IconLoader.getIcon("/icons/start.png", TimerPanel.class));
        }
        isRunning = !isRunning;
    }

    private void resetTimer(ActionEvent e) {
        timer.stop();
        startTime = 0;
        pausedTime = 0;
        isRunning = false;
        label.setText("00:00:00");
        toggleButton.setIcon(IconLoader.getIcon("/icons/start.png", TimerPanel.class));
    }

    private void updateTime(ActionEvent e) {
        long elapsed = System.currentTimeMillis() - startTime;
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;

        label.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void startTimer() {
        if (pausedTime == 0) {
            startTime = System.currentTimeMillis();
        } else {
            startTime = System.currentTimeMillis() - pausedTime;
        }
        timer.start();
    }

    private void pauseTimer() {
        timer.stop();
        pausedTime = System.currentTimeMillis() - startTime;
    }

    public void onActivate() {
        if (!timer.isRunning() && startTime == 0) {
            resetTimer(null);
        }
    }
}
