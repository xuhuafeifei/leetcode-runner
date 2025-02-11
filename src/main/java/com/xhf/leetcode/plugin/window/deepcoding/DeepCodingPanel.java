package com.xhf.leetcode.plugin.window.deepcoding;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.Constants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 深度刷题面板
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DeepCodingPanel extends JPanel {
    private final Project project;
    private final JBEditorTabs tabs;
    private final Hot100Panel hot100Panel;
    private final Interview150Panel interview150Panel;
    private final LCCompetitionPanel lcCompetitionPanel;

    public DeepCodingPanel(Project project, @NotNull Disposable parentDisposable) {
        this.project = project;

        // 创建选项卡 (基于 JBEditorTabs)
        this.tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), parentDisposable);
        this.tabs.setBorder(Constants.BORDER);

        this.hot100Panel = new Hot100Panel(project);
        this.interview150Panel = new Interview150Panel();
        this.lcCompetitionPanel = new LCCompetitionPanel();

        TabInfo hotTab = new TabInfo(hot100Panel);
        hotTab.setText("Hot 100 题");
        hotTab.setIcon(IconLoader.getIcon("/icons/m_hot100.png", this.getClass().getClassLoader()));
        tabs.addTab(hotTab);

        TabInfo interviewTab = new TabInfo(interview150Panel);
        interviewTab.setText("经典面试 150 题");
        interviewTab.setIcon(IconLoader.getIcon("/icons/m_mianshi150.png", this.getClass().getClassLoader()));
        tabs.addTab(interviewTab);

        TabInfo lcCompeTab = new TabInfo(lcCompetitionPanel);
        lcCompeTab.setText("LC-竞赛题");
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
}
