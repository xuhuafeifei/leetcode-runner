package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import com.intellij.ui.JBColor;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.review.utils.MessageReceiveInterface;
import com.xhf.leetcode.plugin.review.utils.ReviewConstants;
import com.xhf.leetcode.plugin.utils.BundleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author feigebuge
 */
public class ReviewWindow extends JFrame implements Disposable, MessageReceiveInterface {
    private final Project project;
    /**
     * 命令行选项卡
     */
    private final JBEditorTabs tabs;

    // jwindow的长/宽比
    public final static float radio = 1.2f;
    public final static int initHeight = 330;

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
            }
        });

        this.setVisible(true);
    }


    private JPanel createMainPanel() {
        var mainPanel = new JPanel();
        var env = new ReviewEnv();
        env.registerListener(this);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
        mainPanel.setBackground(JBColor.background());
        mainPanel.setOpaque(true); // 确保背景不透明

        // tabs
        tabs.addTab(createDailyPlanTabInfo(env));
        tabs.addTab(createTotalReviewPlanTabInfo(env));
        tabs.addTab(createSettingsTabInfo(env));

        mainPanel.add(tabs, BorderLayout.CENTER);

        return mainPanel;
    }

    @Override
    public void onMessageReceived(String msg) {
        if (ReviewConstants.CLOSE_WINDOW.equals(msg)) {
            ActionUtils.disposeReviewWindow();
        }
    }

    private TabInfo createDailyPlanTabInfo(ReviewEnv env) {
        JPanel panel  = new DailyPlanTabPanel(project, env);
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(BundleUtils.i18n("action.leetcode.review.dailyPlan"));
        return tabInfo;
    }
private TabInfo createTotalReviewPlanTabInfo(ReviewEnv env) {
    JPanel panel  = new TotalReviewPlanTabPanel(project, env);
    TabInfo tabInfo = new TabInfo(panel);
    tabInfo.setText(BundleUtils.i18n("action.leetcode.review.totalReview"));
    return tabInfo;
}

private TabInfo createSettingsTabInfo(ReviewEnv env) {
    JPanel panel = new SettingsTabPanel(project);
    TabInfo tabInfo = new TabInfo(panel);
    tabInfo.setText(BundleUtils.i18nHelper("设置", "Settings"));
    return tabInfo;
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
        Disposer.dispose(this);
        super.dispose();
    }

}
