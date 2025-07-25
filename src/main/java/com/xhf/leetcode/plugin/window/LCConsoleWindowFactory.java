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
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCConsoleWindowFactory implements ToolWindowFactory, DumbAware {

    // 使用final修饰
    public final static String LEETCODE_CONSOLE_PLUGIN_ID = "Leetcode Runner Console";

    public static DataContext getDataContext(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project)
            .getToolWindow(LEETCODE_CONSOLE_PLUGIN_ID);
        if (leetcodeToolWindows == null) {
            ViewUtils.showDialog(project, LEETCODE_CONSOLE_PLUGIN_ID
                + " 工具窗口获取失败\n, 请通过 'View->Tool Windows->Leetcode Runner 控制台' 打开", "提示");
            throw new RuntimeException(LEETCODE_CONSOLE_PLUGIN_ID + " 获取失败");
        }
        LCConsolePanel LCConsolePanel = (LCConsolePanel) Objects.requireNonNull(
            leetcodeToolWindows.getContentManager().getContent(0)).getComponent();
        return DataManager.getInstance().getDataContext(LCConsolePanel);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LCConsolePanel LCConsolePanel = new LCConsolePanel(toolWindow, project);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory().createContent(LCConsolePanel, "", false);
        contentManager.addContent(content);
    }
}
