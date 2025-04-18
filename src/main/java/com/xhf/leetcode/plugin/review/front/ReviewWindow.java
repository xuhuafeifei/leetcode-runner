package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import com.intellij.ui.JBColor;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author feigebuge
 */
public class ReviewWindow extends JFrame implements Disposable {
    private final Project project;
    /**
     * 命令行选项卡
     */
    private final JBEditorTabs tabs;

    // jwindow的长/宽比
    public final static float radio = 1.2f;
    public final static int initHeight = 395;

    public ReviewWindow(Project project) {
        this.project = project;
        this.tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), this);

        // 窗口配置
        setAlwaysOnTop(true);
        setTitle(BundleUtils.i18n("action.leetcode.review.title"));
        setContentPane(createMainPanel());
        setGlassPane(new IdeGlassPaneImpl(new JRootPane()));
        pack();
        setSize((int) (initHeight * radio), initHeight);

        centerWindow();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // 添加窗口监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 调用原有的关闭逻辑
                ActionUtils.disposeReviewWindow();
                // 关闭窗口
                dispose();
            }
        });

        this.setVisible(true);
    }


    private JPanel createMainPanel() {
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
        mainPanel.setBackground(JBColor.background());
        mainPanel.setOpaque(true); // 确保背景不透明

        // tabs
        tabs.addTab(createDailyPlanTabInfo());
        tabs.addTab(createTotalReviewPlanTabInfo());

        mainPanel.add(tabs, BorderLayout.CENTER);

        return mainPanel;
    }

    private TabInfo createDailyPlanTabInfo() {
        JPanel panel  = new DailyPlanTabPanel(project);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.dailyPlan"));
        return tabInfo;
    }

    private TabInfo createTotalReviewPlanTabInfo() {
        JPanel panel  = new TotalReviewPlanTabPanel(project);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.totalReview"));
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
