package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.CodeSubmitEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.bus.LoginEvent;
import com.xhf.leetcode.plugin.bus.QLoadEndEvent;
import com.xhf.leetcode.plugin.bus.QLoadStartEvent;
import com.xhf.leetcode.plugin.bus.RePositionEvent;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.listener.QuestionListener;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.render.QuestionCellRender;
import com.xhf.leetcode.plugin.search.engine.QuestionEngine;
import com.xhf.leetcode.plugin.search.engine.SearchEngine;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.filter.FilterChain;
import com.xhf.leetcode.plugin.window.filter.QFilterChain;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 搜索面板, 提供题目搜索的能力. 内部封装了一个搜索引擎, 提供高效的搜索能力
 */
@LCSubscriber(events = {LoginEvent.class, ClearCacheEvent.class, CodeSubmitEvent.class, QLoadStartEvent.class,
    QLoadEndEvent.class})
public class SearchPanel extends AbstractSearchPanel<Question> {

    private final QuestionEngine engine;
    private final Project project;
    private final QFilterChain filterChain;
    private MyList<Question> questionList;

    public SearchPanel(Project project) {
        super(project);
        this.engine = QuestionEngine.getInstance(project);
        this.project = project;
        this.questionList = new MyList<>();
        initMyList();
        this.filterChain = new QFilterChain();
        LCEventBus.getInstance().register(this);
        super.init();
    }

    @Override
    protected MyList<Question> getDataList() {
        return questionList;
    }

    @Override
    protected SearchEngine<Question> getSearchEngine() {
        return engine;
    }

    @Override
    protected FilterChain<Question> getFilterChain() {
        return filterChain;
    }

    @Override
    protected List<MySearchConditionPanel<Question>> getSearchCondition() {
        List<MySearchConditionPanel<Question>> list = new ArrayList<>();
        list.add(super.initDifficultyCond());
        list.add(super.initStatusCond());
        list.add(super.initCategoryCond());
        list.add(super.initTagsCond());
        return list;
    }

    protected void initMyList() {
        // build question list
        questionList = new MyList<>();
        questionList.setCellRenderer(new QuestionCellRender());
        questionList.addMouseListener(new QuestionListener(questionList, project));
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.login.required")
        );

        JBScrollPane jbScrollPane = new JBScrollPane(questionList);
        this.add(jbScrollPane, BorderLayout.CENTER);
        this.setContent(jbScrollPane);
    }

    // 为搜索引擎提供数据源, 同时创建索引
    public void buildIndex(java.util.List<Question> source) {
        try {
            engine.buildIndex(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<Question> getUpdateData() {
        return QuestionService.getInstance(project).getTotalQuestion(project);
    }

    public MyList<Question> getMyList() {
        return questionList;
    }

    @Subscribe
    public void loginEventListener(LoginEvent listener) {
        // 提前调用indexLock(). 因为登录后必定要重新加载所有题目数据, 从而rebuild engine's index
        indexLock();
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.search.loading"));
        // questionList.setEmptyText("数据加载中, 请稍等");
    }

    @Subscribe
    public void qLoadStartListener(QLoadStartEvent event) {
        indexLock();
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.search.loading"));
        // questionList.setEmptyText("数据加载中, 请稍等");
        questionList.setNonData();
    }

    /**
     * 当数据加载完毕后, 执行渲染逻辑
     *
     * @param event event
     */
    @Subscribe
    public void qLoadEndListener(QLoadEndEvent event) {
        unLock();
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.search.noData"));
        // questionList.setEmptyText("没有可用于展示的数据...");
        List<Question> totalQuestion = QuestionService.getInstance(project).getTotalQuestion(project);
        questionList.setListData(totalQuestion);
        buildIndex(totalQuestion);
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        AtomicReference<Boolean> state = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            state.set(LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CODING_STATE));
        });
        // state为true, 正常显示; 否则是deep coding显示模式, 不能在SearchPanel定位
        if (!Boolean.TRUE.equals(state.get())) {
            return;
        }
        indexLock();
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.search.loading"));
        questionList.setNonData();
        buildIndex(QuestionService.getInstance(project).getTotalQuestion(project));
        unLock();
        updateText();
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.login.required"));
        // questionList.setEmptyText("请先登录...");
        questionList.setNonData();
    }

    @Subscribe
    public void rePositionEventListeners(RePositionEvent event) {
        AtomicReference<Boolean> state = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            state.set(LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CODING_STATE));
        });
        // state为true, 正常显示; 否则是deep coding显示模式, 不能在SearchPanel定位
        if (!Boolean.TRUE.equals(state.get())) {
            return;
        }
        // 这里需要清除searchPanel设置的搜索条件, 不然查询到的数据是缺失的
        this.clear();

        ViewUtils.rePosition(event, questionList, project);
    }
}
