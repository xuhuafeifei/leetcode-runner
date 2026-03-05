package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.TimeStopEvent;
import com.xhf.leetcode.plugin.setting.InnerHelpTooltip;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * @author 文艺倾年
 */
public class TimerWindow extends JWindow {

    private static final float SCALE_PER = 0.02f; // 每次滚动调整的像素值百分比
    // 计时器组件
    private final Timer timer;
    // jwindow的长/宽比
    private final float radio = 1.5f;
    private final int miniHeight = 85;
    private final String HELP_CONTENT = BundleUtils.i18n("action.leetcode.timer.help");
    private long startTime;
    private long pausedTime;
    private boolean isRunning;
    // UI组件
    private JBLabel timeLabel;
    private JButton startOrPauseBtn;
    private Point dragPoint;
    private String lastTime = "00:00:00";

    public TimerWindow() {
        // 初始化计时器状态
        timer = new Timer(50, this::updateTime);
        startTime = 0;
        pausedTime = 0;
        isRunning = false;

        // 窗口配置
        setAlwaysOnTop(true);
        setBackground(new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)));
        setContentPane(createMainPanel());
        pack();
        int initHeight = 100;
        setSize((int) (initHeight * radio), initHeight);

        // 交互功能
        setupDragSupport();
        setupResizeSupport();
        centerWindow();

        LCEventBus.getInstance().register(this);
    }

    private void setupResizeSupport() {
        // 添加鼠标滚轮监听器
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation(); // 获取滚动方向
                int width = getWidth();
                int height = getHeight();
                Point location = getLocation();

                // 根据滚动方向调整窗口大小
                if (rotation < 0) { // 向上滚动，放大
                    height += (int) (height * SCALE_PER);
                    width += (int) (width * SCALE_PER);
                } else if (rotation > 0) { // 向下滚动，缩小
                    height = (int) Math.max(height - (height * SCALE_PER), miniHeight);
                    width = (int) Math.max(width - (width * SCALE_PER), (int) (miniHeight * radio));
                }

                // 计算新的窗口位置，保持窗口中心不变
                int newWidth = width;
                int newHeight = height;
                int newX = location.x - (newWidth - getWidth()) / 2;
                int newY = location.y - (newHeight - getHeight()) / 2;

                // 更新窗口大小和位置
                setBounds(newX, newY, newWidth, newHeight);
            }
        });
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        // 不要增加这行代码, 会导致在old UI渲染时, 颜色冲突
        // mainPanel.setBackground(JBColor.background());
        mainPanel.setOpaque(true); // 确保背景不透明

        // 标题栏
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // 时间显示
        JPanel timePanel = new JPanel(new BorderLayout());
        // 不要增加这行代码, 会导致在old UI渲染时, 颜色冲突
        // timePanel.setBackground(JBColor.background());

        timeLabel = new JBLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timePanel.add(timeLabel, BorderLayout.CENTER);

        mainPanel.add(timePanel, BorderLayout.CENTER);

        mainPanel.add(createControlPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        // 不要增加这行代码, 会导致在old UI渲染时, 颜色冲突
        // titleBar.setBackground(JBColor.background());
        titleBar.setPreferredSize(new Dimension(0, 30));

        // 关闭按钮
        JButton closeBtn = createIconButton("/icons/clean.svg", e -> ActionUtils.disposeTimer());

        // 标题文本
        JLabel title = new JLabel(BundleUtils.i18n("action.leetcode.timer.title"));
        title.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        titleBar.add(
            InnerHelpTooltip.FlowLayout(FlowLayout.LEADING).add(title).addHelp(this.HELP_CONTENT).getTargetComponent(),
            BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        return titleBar;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 启动/暂停按钮
        startOrPauseBtn = createIconButton("/icons/start.svg", e -> toggleTimer());
        startOrPauseBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JButton resetBtn = createIconButton("/icons/reset.svg", e -> resetTimer());
        resetBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        panel.add(startOrPauseBtn, BorderLayout.WEST);
        panel.add(resetBtn, BorderLayout.EAST);

        return panel;
    }

    private JButton createIconButton(String iconPath, ActionListener listener) {
        Icon icon = IconLoader.getIcon(iconPath, TimerWindow.class);
        return createIconButton(icon, listener);
    }

    private JButton createIconButton(Icon icon, ActionListener listener) {
        var btn = new JButton(icon);
        btn.setBorder(BorderFactory.createEmptyBorder());
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

    /**
     * code service提交代码显示结果时需要使用
     */
    public void pauseTimer() {
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
            isRunning ? "/icons/pause.svg" : "/icons/start.svg",
            getClass()
        ));
    }

    private void updateTime(ActionEvent e) {
        long elapsed = System.currentTimeMillis() - startTime;
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;
        String curtime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        lastTime = curtime == null ? "" : curtime;
        timeLabel.setText(curtime);
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
            (screen.height - getHeight()) / 4
        );
    }

    @Override
    public void dispose() {
        LCEventBus.getInstance().remove(this);
        timer.stop();
        super.dispose();
    }

    public String getTime() {
        return lastTime;
    }

    @Subscribe
    public void TimeStopListeners(TimeStopEvent event) {
        this.pauseTimer();
        startOrPauseBtn.setIcon(IconLoader.getIcon(
            isRunning ? "/icons/pause.svg" : "/icons/start.svg",
            getClass()
        ));
    }
}
