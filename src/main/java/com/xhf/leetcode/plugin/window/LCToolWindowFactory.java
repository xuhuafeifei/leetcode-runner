package com.xhf.leetcode.plugin.window;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCToolWindowFactory implements ToolWindowFactory, DumbAware {

    public final static String LEETCODE_RUNNER_ID = "Leetcode Runner";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LCPanel lcPanel = new LCPanel(toolWindow, project);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory().createContent(lcPanel, "", false);
        contentManager.addContent(content);
    }

    public static DataContext getDataContext(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(LEETCODE_RUNNER_ID);
        if (leetcodeToolWindows == null) {
            ViewUtils.showDialog(project, LEETCODE_RUNNER_ID+ " 工具窗口获取失败\n, 请通过 'View->Tool Windows->Leetcode Runner' 打开");
            throw new RuntimeException(LEETCODE_RUNNER_ID + " 获取失败");
        }
        LCPanel lcPanel = (LCPanel) Objects.requireNonNull(leetcodeToolWindows.getContentManager().getContent(0)).getComponent();
        return DataManager.getInstance().getDataContext(lcPanel);
    }
}
