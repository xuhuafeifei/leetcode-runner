package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.xhf.leetcode.plugin.bus.DeepCodingEvent;
import com.xhf.leetcode.plugin.bus.DeepCodingTabChooseEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.service.LoginService;
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
    private DeepCodingPanel deepCodingPanel;
    private final Project project;
    private final SearchPanel searchPanel;
    /**
     * 表示当前显示状态, 如果是true, 则显示正常界面, 否则显示deep coding界面
     */
    private Boolean state = true;

    public LCPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();

        // get action toolbar
        DefaultActionGroup dag = (DefaultActionGroup) actionManager.getAction("leetcode.plugin.lcActionsToolbar");
        AnAction dailyAction = actionManager.getAction("leetcode.plugin.TodayQuestionAction");

        this.actionToolbar = actionManager.createActionToolbar("leetcode Toolbar", dag, true);

        initLeetcodeClient();

        // search panel
        searchPanel = new SearchPanel(project);

        setDefaultContent();

        // 判断是否登录成功
        LoginService loginService = LoginService.getInstance(project);
        if (loginService.isLogin()) {
            loginService.doLoginAfter();
        }

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
            if (deepCodingPanel == null) {
                return null;
            }
            return deepCodingPanel.getHot100Data();
        } else if (DataKeys.LEETCODE_DEEP_CODING_INTERVIEW_100_QUESTION_LIST.is(dataId)) {
            if (deepCodingPanel == null) {
                return null;
            }
            return deepCodingPanel.getInterview150Data();
        } else if (DataKeys.LEETCODE_DEEP_CODING_LC_COMPETITION_QUESTION_LIST.is(dataId)) {
            if (deepCodingPanel == null) {
                return null;
            }
            return deepCodingPanel.getLcCompetitionData();
        } else if (DataKeys.LEETCODE_CODING_STATE.is(dataId)) {
            return state;
        } else if (DataKeys.LEETCODE_CHOOSEN_TAB_NAME.is(dataId)) {
            if (deepCodingPanel == null) {
                return null;
            }
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
        // deep coding
        if (deepCodingPanel == null) {
            // todo: 需要考虑并发问题吗?
            // 目前来看不存在并发问题, 一方面是有较为严格的限流, 另一方面所有交互都是通过人手点击的, 并不会有并发问题
            // lazy init
            this.deepCodingPanel = new DeepCodingPanel(project, this);
        }
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
