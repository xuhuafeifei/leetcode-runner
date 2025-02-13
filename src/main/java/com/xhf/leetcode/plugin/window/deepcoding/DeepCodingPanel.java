package com.xhf.leetcode.plugin.window.deepcoding;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 深度刷题面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DeepCodingPanel extends JPanel {
    /**
     * 承载不同tab的容器
     */
    private final JBEditorTabs tabs;
    /**
     * leetcode 热题 100道
     */
    private final Hot100Panel hot100Panel;
    /**
     * leetcode 经典面试 150道
     */
    private final Interview150Panel interview150Panel;
    /**
     * leetcode 竞赛题目
     */
    private final LCCompetitionPanel lcCompetitionPanel;
    private final TabInfo hotTab;
    private final TabInfo interviewTab;
    private final TabInfo lcCompeTab;

    public DeepCodingPanel(Project project, @NotNull Disposable parentDisposable) {

        // 创建选项卡 (基于 JBEditorTabs)
        this.tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), parentDisposable);
        this.tabs.setBorder(Constants.BORDER);

        this.hot100Panel = new Hot100Panel(project);
        this.interview150Panel = new Interview150Panel(project);
        this.lcCompetitionPanel = new LCCompetitionPanel(project);

        this.hotTab = new TabInfo(hot100Panel);
        hotTab.setText(Hot100Panel.HOT_100_TEXT);
        hotTab.setIcon(IconLoader.getIcon("/icons/m_hot100.png", this.getClass().getClassLoader()));
        tabs.addTab(hotTab);

        this.interviewTab = new TabInfo(interview150Panel);
        interviewTab.setText(Interview150Panel.INTERVIEW_150_TEXT);
        interviewTab.setIcon(IconLoader.getIcon("/icons/m_mianshi150.png", this.getClass().getClassLoader()));
        tabs.addTab(interviewTab);

        this.lcCompeTab = new TabInfo(lcCompetitionPanel);
        lcCompeTab.setText(LCCompetitionPanel.LC_COMPETITION_TEXT);
        lcCompeTab.setIcon(IconLoader.getIcon("/icons/m_LeetCode_Cup.png", this.getClass().getClassLoader()));
        tabs.addTab(lcCompeTab);

        add(tabs);
    }

    public JBEditorTabs getTabs() {
        return tabs;
    }

    public MyList<Question> getHot100Data() {
        return hot100Panel.getDataList();
    }

    public MyList<Question> getInterview150Data() {
        return interview150Panel.getDataList();
    }

    public MyList<CompetitionQuestion> getLcCompetitionData() {
        return lcCompetitionPanel.getDataList();
    }

    /**
     * 获取当前正在显示tab的显示名称
     */
    public String getCurrentTab() {
        TabInfo selectedInfo = tabs.getSelectedInfo();
        if (selectedInfo == null) {
            return "";
        }
        return selectedInfo.getText();
    }

    /**
     * 根据tab名称设置显示的tab
     */
    public void setTab(String tabName) {
        switch (tabName) {
            case Hot100Panel.HOT100:
                tabs.select(this.hotTab, true);
                break;
            case Interview150Panel.INTER150:
                tabs.select(this.interviewTab, true);
                break;
            case LCCompetitionPanel.LC_COMPETITION:
                tabs.select(this.lcCompeTab, true);
                break;
            default:
                LogUtils.warn("setTab error, 未知的tabName! tabName = " + tabName);
                break;
        }
    }
}
