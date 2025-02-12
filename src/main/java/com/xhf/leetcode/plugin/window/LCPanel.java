package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.xhf.leetcode.plugin.bus.DeepCodingEvent;
import com.xhf.leetcode.plugin.bus.DeepCodingTabChooseEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.deepcoding.DeepCodingPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCPanel extends SimpleToolWindowPanel implements DataProvider, Disposable {
    private final ActionToolbar actionToolbar;
    private final DeepCodingPanel deepCodingPanel;
    private Project project;
    private SearchPanel searchPanel;
    /**
     * 表示当前显示状态, 如果是true, 则显示正常界面, 否则显示deep coding界面
     */
    private Boolean state = true;

    public LCPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();

        // get action toolbar
        this.actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.plugin.lcActionsToolbar"),
                true);

        initLeetcodeClient();

        // search panel
        searchPanel = new SearchPanel(project);
        // deep coding
        this.deepCodingPanel = new DeepCodingPanel(project, this);

        setDefaultContent();

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
        } else if (DataKeys.LEETCODE_DEEP_CODING_HOT_100_QUESTION_LIST.is(dataId)) {
            return deepCodingPanel.getHot100Data();
        } else if (DataKeys.LEETCODE_DEEP_CODING_INTERVIEW_100_QUESTION_LIST.is(dataId)) {
            return deepCodingPanel.getInterview150Data();
        } else if (DataKeys.LEETCODE_CODING_STATE.is(dataId)) {
            return state;
        } else if (DataKeys.LEETCODE_CHOOSEN_TAB_NAME.is(dataId)) {
            return deepCodingPanel.getCurrentTab();
        }
        return null;
    }

    /**
     * 监听 deep coding 切换 事件
     */
    @Subscribe
    public void deepCodingEventListener(DeepCodingEvent event) {
        state = !state;
        if (state) {
            setDefaultContent();
        } else {
            setDeepCodingContent();
        }
    }

    /**
     * 监听 deep coding tab 选择事件
     */
    @Subscribe
    public void deepCodingTabChooseEventListener(DeepCodingTabChooseEvent event) {
        state = ! state;
        setDeepCodingContent();
        deepCodingPanel.setTab(event.getPattern());
    }

    private void setDeepCodingContent() {
        // store to action toolbar
        actionToolbar.setTargetComponent(deepCodingPanel.getTabs());
        setToolbar(actionToolbar.getComponent());
        setContent(deepCodingPanel.getTabs());
    }

    private void setDefaultContent() {
        // store to action toolbar
        actionToolbar.setTargetComponent(searchPanel.getMyList());
        setToolbar(actionToolbar.getComponent());
        setContent(searchPanel);
    }

    @Override
    public void dispose() {

    }
}
