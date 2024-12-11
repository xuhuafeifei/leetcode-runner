package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.utils.DataKeys;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCPanel extends SimpleToolWindowPanel implements DataProvider {
    private Project project;
    private SearchPanel searchPanel;

    public LCPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();

        // get action toolbar
        ActionToolbar actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.plugin.lcActionsToolbar"),
                true);

        initLeetcodeClient();

        // search panel
        searchPanel = new SearchPanel(project);

        // store to action toolbar
        actionToolbar.setTargetComponent(searchPanel.getMyList());
        setToolbar(actionToolbar.getComponent());
        setContent(searchPanel);

        // register
        LCEventBus.getInstance().register(this);
    }

    private void initLeetcodeClient() {
        LeetcodeClient.init(this.project);
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_QUESTION_LIST.is(dataId)) {
            return searchPanel.getMyList();
        }
        return null;
    }
}
