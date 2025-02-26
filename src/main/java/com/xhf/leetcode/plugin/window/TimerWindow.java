package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author 文艺倾年
 */
public class TimerWindow extends JWindow {
    // 计时器组件
    private final Timer timer;
    private long startTime;
    private long pausedTime;
    private boolean isRunning;

    // UI组件
    private JBLabel timeLabel;
    private JButton startOrPauseBtn;
    private JButton resetBtn;
    private Point dragPoint;

    public TimerWindow(Project project) {
        // 初始化计时器状态
        timer = new Timer(50, this::updateTime);
        startTime = 0;
        pausedTime = 0;
        isRunning = false;

        // 窗口配置
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(createMainPanel());
        pack();
        setSize(300, 150);

        // 交互功能
        setupDragSupport();
        centerWindow();
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
        mainPanel.setBackground(JBColor.background());
        mainPanel.setOpaque(true); // 确保背景不透明

        // 标题栏
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // 时间显示
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        timePanel.setBackground(JBColor.background());

        timeLabel = new JBLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        timePanel.add(timeLabel, BorderLayout.CENTER);

        mainPanel.add(timePanel, BorderLayout.CENTER);

        // 控制按钮
        JPanel controlPanel = createControlPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        mainPanel.add(createControlPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(JBColor.background());
        titleBar.setPreferredSize(new Dimension(0, 30));

        // 关闭按钮
        JButton closeBtn = new JButton(IconLoader.getIcon("/icons/clean.svg", getClass()));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        closeBtn.addActionListener(e -> dispose());

        // 标题文本
        JLabel title = new JLabel("Leetcode Timer");
        title.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        return titleBar;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);

        // 启动/暂停按钮
        startOrPauseBtn = createIconButton("/icons/start.svg", e -> toggleTimer());
        resetBtn = createIconButton("/icons/reset.svg", e -> resetTimer());

        panel.add(startOrPauseBtn);
        panel.add(resetBtn);

        return panel;
    }

    private JButton createIconButton(String iconPath, ActionListener listener) {
        Icon icon = IconLoader.getIcon(iconPath, TimerWindow.class);
        JButton btn = new JButton(icon);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setContentAreaFilled(false);
        btn.addActionListener(listener);
        return btn;
    }

    private void toggleTimer() {
        if (!isRunning) {
            startTimer();
        } else {
            pauseTimer();
        }
        updateButtonStates();
    }

    private void startTimer() {
        if (pausedTime == 0) {
            startTime = System.currentTimeMillis();
        } else {
            startTime = System.currentTimeMillis() - pausedTime;
        }
        timer.start();
        isRunning = true;
    }

    private void pauseTimer() {
        timer.stop();
        pausedTime = System.currentTimeMillis() - startTime;
        isRunning = false;
    }

    private void resetTimer() {
        timer.stop();
        startTime = 0;
        pausedTime = 0;
        isRunning = false;
        timeLabel.setText("00:00:00");
        updateButtonStates();
    }

    private void updateButtonStates() {
        startOrPauseBtn.setIcon(IconLoader.getIcon(
                isRunning ? "/icons/pause.png" : "/icons/start.png",
                getClass()
        ));
    }

    private void updateTime(ActionEvent e) {
        long elapsed = System.currentTimeMillis() - startTime;
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;
        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void setupDragSupport() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point current = getLocation();
                int deltaX = e.getX() - dragPoint.x;
                int deltaY = e.getY() - dragPoint.y;
                setLocation(current.x + deltaX, current.y + deltaY);
            }
        });
    }

    private void centerWindow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 2
        );
    }

    @Override
    public void dispose() {
        timer.stop();
        super.dispose();
    }

}
