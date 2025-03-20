package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import com.intellij.ui.JBColor;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author feigebuge
 */
public class ReviewWindow extends JFrame {
    private final Project project;
    /**
     * 命令行选项卡
     */
    private final JBTabsImpl tabs;

    // jwindow的长/宽比
    private final float radio = 1.2f;

    public ReviewWindow(Project project) {
        this.project = project;
        this.tabs = new JBTabsImpl(project);

        // 窗口配置
        setAlwaysOnTop(true);
        setTitle(BundleUtils.i18n("action.leetcode.review.title"));
        setContentPane(createMainPanel());
        setGlassPane(new IdeGlassPaneImpl(new JRootPane()));
        pack();
        int initHeight = 380;
        setSize((int) (initHeight * radio), initHeight);

        centerWindow();

        this.setVisible(true);
    }


    private JPanel createMainPanel() {
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
        mainPanel.setBackground(JBColor.background());
        mainPanel.setOpaque(true); // 确保背景不透明

        // tabs
        tabs.addTab(createReviewTabInfo());
        tabs.addTab(createRecordTabInfo());

        mainPanel.add(tabs, BorderLayout.CENTER);

        return mainPanel;
    }

    private TabInfo createRecordTabInfo() {
        JPanel panel  = new DailyPlanTabPanel(project);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.recordTxt"));
        return tabInfo;
    }

    private TabInfo createReviewTabInfo() {
        JPanel panel  = new ReviewTabPanel(project);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.reviewTxt"));
        return tabInfo;
    }


    private void centerWindow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 4
        );
    }
}
