package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.setting.InnerHelpTooltip;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author feigebuge
 */
public class ReviewWindow extends JWindow implements Disposable {
    private final Project project;
    /**
     * 命令行选项卡, 用于呈现Console 和 Variables 选项卡
     */
    private final JBEditorTabs tabs;

    // UI组件
    private Point dragPoint;

    // jwindow的长/宽比
    private final float radio = 1.2f;
    private final int miniHeight = 350;

    private final String HELP_CONTENT = BundleUtils.i18n("action.leetcode.review.help");

    public ReviewWindow(Project project) {
        this.project = project;
        this.tabs    = new JBEditorTabs(project, IdeFocusManager.getInstance(project), this);
        // 窗口配置
        setAlwaysOnTop(true);
        setBackground(new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)));
        setContentPane(createMainPanel());
        pack();
        int initHeight = 350;
        setSize((int) (initHeight * radio), initHeight);

        // 交互功能
        setupDragSupport();
        setupResizeSupport();
        centerWindow();
    }

    private static final float SCALE_PER = 0.02f; // 每次滚动调整的像素值百分比

    private void setupResizeSupport() {
        // 添加鼠标滚轮监听器
        addMouseWheelListener(e -> {
            // 这块代码和TimerWindow一致, 之所以不做额外提取, 是考虑到我要让
            // review模块和别的模块独立, 尽可能减小交集
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
        });
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
        mainPanel.setBackground(JBColor.background());
        mainPanel.setOpaque(true); // 确保背景不透明

        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // tabs
        tabs.addTab(createReviewTabInfo());
        tabs.addTab(createRecordTabInfo());

        mainPanel.add(tabs, BorderLayout.CENTER);

        return mainPanel;
    }

    private TabInfo createRecordTabInfo() {
        JPanel panel  = new RecordTabPanel(project);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.recordTxt"));
        return tabInfo;
    }

    private TabInfo createReviewTabInfo() {
        JPanel panel  = new DailyPlanTabPanel(project);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.reviewTxt"));
        return tabInfo;
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(0, 30));

        // 关闭按钮
        JButton closeBtn = createIconButton("/icons/clean.svg", e -> ActionUtils.disposeReviewWindow());

        // 标题文本
        JLabel title = new JLabel(BundleUtils.i18n("action.leetcode.review.title"));
        title.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        titleBar.add(InnerHelpTooltip.FlowLayout(FlowLayout.LEADING).add(title).addHelp(this.HELP_CONTENT).getTargetComponent(), BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        return titleBar;
    }

    private JButton createIconButton(String iconPath, ActionListener listener) {
        Icon icon = IconLoader.getIcon(iconPath, ReviewWindow.class);
        return createIconButton(icon, listener);
    }

    private JButton createIconButton(Icon icon, ActionListener listener) {
        var btn = new JButton(icon);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12 / 2, 8, 12 / 2));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.addActionListener(listener);
        return btn;
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
        super.dispose();
    }
}
