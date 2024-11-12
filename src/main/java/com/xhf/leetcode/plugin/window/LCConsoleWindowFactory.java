package com.xhf.leetcode.plugin.window;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class LCConsoleWindowFactory implements ToolWindowFactory, DumbAware {

    public static String ID = "Leetcode Console";


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LCConsolePanel LCConsolePanel = new LCConsolePanel(toolWindow, project);
        Content content = ContentFactory.SERVICE.getInstance().createContent(LCConsolePanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static DataContext getDataContext(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        LCConsolePanel LCConsolePanel = (LCConsolePanel) leetcodeToolWindows.getContentManager().getContent(0).getComponent();
        return DataManager.getInstance().getDataContext(LCConsolePanel);
    }
}
