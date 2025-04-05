package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.execution.ui.ConsoleViewContentType;
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
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.TaskCenter;
import com.xhf.leetcode.plugin.window.deepcoding.DeepCodingPanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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

        try {
            init();
        } catch (Exception e) {
            DebugUtils.simpleDebug(BundleUtils.i18nHelper("Leetcode-Runner 初始化异常!", "Leetcode-Runner init error!"),
                    project, ConsoleViewContentType.ERROR_OUTPUT, true
                    );
        }

        // get action toolbar
        DefaultActionGroup dag = (DefaultActionGroup) actionManager.getAction("leetcode.plugin.lcActionsToolbar");
        this.actionToolbar = actionManager.createActionToolbar("leetcode Toolbar", dag, true);

        // search panel
        searchPanel = new SearchPanel(project);

        setDefaultContent();

        // 判断是否登录成功
        LoginService loginService = LoginService.getInstance(project);
        if (loginService.isLogin()) {
            loginService.doLoginAfter();
        }

        backgroundCheck();

        // register
        LCEventBus.getInstance().register(this);
    }

    private void backgroundCheck() {
        // 检测存储路径是否正常
        AppSettings appSettings = AppSettings.getInstance();
        String filePath = appSettings.getFilePath();
        if (StringUtils.isNotBlank(filePath)) {
            TaskCenter.getInstance().createTask(() -> {
                // 创建临时文件夹
                try {
                    // 在filePath下创建文件
                    File file = FileUtils.createAndGetFile(new FileUtils.PathBuilder(filePath).append("temp-file.txt").build());
                    // 删除文件
                    FileUtils.removeFile(file.getAbsolutePath());
                } catch (Exception e) {
                    ConsoleUtils.getInstance(project).showWaring(
                            BundleUtils.i18nHelper(
                                    "您的存储路径存在异常, Runner无法正确创建文件, 请检查路径权限! filePath = " + filePath,
                                    "Your storage path has an abnormality, Runner cannot create the file correctly, please check the path permission! filePath = " + filePath
                            ),
                            false, false
                    );
                }
            }).invokeLater();
        }
        // 检测缓存存储路径是否异常
        String path = appSettings.getCoreFilePath();
        if (StringUtils.isNotBlank(path)) {
            TaskCenter.getInstance().createTask(() -> {
                // 创建缓存文件夹
                try {
                    // 在filePath下创建文件
                    File file = FileUtils.createAndGetFile(new FileUtils.PathBuilder(path).append("temp-file.txt").build());
                    // 删除文件
                    FileUtils.removeFile(file.getAbsolutePath());
                } catch (Exception e) {
                    ConsoleUtils.getInstance(project).showError(
                            BundleUtils.i18nHelper(
                                    "您的缓存存储路径存在异常, Runner无法正确写入缓存文件, 请清除缓存后重试! core_path = " + path
                                            + "\n" + "如果您需要更详细的信息, 可以参考: https://itxaiohanglover.github.io/leetcode-runner-doc/pages/9cc27d/#_11-%E7%B3%BB%E7%BB%9F%E5%88%9B%E5%BB%BA%E4%BB%A3%E7%A0%81%E6%96%87%E4%BB%B6%E5%A4%B1%E8%B4%A5",
                                    "Your cache storage path has an abnormality, Runner cannot write the cache file correctly, please clear the cache and try again! core_path = " + path
                                            + "\n" + "If you need more detailed information, you can refer to: https://itxaiohanglover.github.io/leetcode-runner-doc/pages/9cc27d/#_11-%E7%B3%BB%E7%BB%9F%E5%88%9B%E5%BB%BA%E4%BB%A3%E7%A0%81%E6%96%87%E4%BB%B6%E5%A4%B1%E8%B4%A5"
                            ),
                            false, false
                    );
                }
            }).invokeLater();
        }
    }

    private void init() {
        LeetcodeClient.init(this.project);
        QuestionService.init(this.project);
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
