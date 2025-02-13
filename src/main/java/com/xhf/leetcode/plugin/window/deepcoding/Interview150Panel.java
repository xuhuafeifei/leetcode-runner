package com.xhf.leetcode.plugin.window.deepcoding;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.search.engine.QuestionEngine;
import com.xhf.leetcode.plugin.search.engine.SearchEngine;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.ArrayUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.AbstractSearchPanel;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import com.xhf.leetcode.plugin.window.deepcoding.filter.DCAlgorithmFilter;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.FilterChain;
import com.xhf.leetcode.plugin.window.filter.QFilterChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Interview150Panel extends AbstractSearchPanel<Question> {
    /**
     * 用于 dci 存储的信息
     */
    public static final String INTER150 = "INTERVIEW_150";
    /**
     * 用于 panel tab显示的信息
     */
    public static final String INTERVIEW_150_TEXT = "经典面试 150 题";
    private final MyList<Question> questionList;
    private final QuestionEngine searchEngine;
    private final QFilterChain filterChain;
    private final Project project;
    // 经典面试150题
    private List<Question> inter150;

    public Interview150Panel(Project project) {
        super(project);
        this.project = project;
        this.questionList = new MyList<Question>();
        this.searchEngine = QuestionEngine.getInstance(project);
        this.filterChain = new QFilterChain();
        initMyList();
        LCEventBus.getInstance().register(this);
        super.init();
        // 一定要unLock. 因为deepCoding功能一定是登陆后才开放, 无需进行锁定
        super.unLock();
    }

    private void initMyList() {
        List<Question> totalQuestion = QuestionService.getInstance().getTotalQuestion(project);
        int[] inter150Id = getInter150();
        this.inter150 = new ArrayList<>(180);
        for (int idx : inter150Id) {
            inter150.add(totalQuestion.get(idx));
        }

        super.initMyListHelper(this.questionList, this.inter150, INTER150);
    }

    private int[] getInter150() {
        // 缓存面试150题的下标
        return new int[] {87,26,25,79,168,188,120,121,54,44,273,379,237,133,134,41,12,11,57,13,150,5,27,67,124,391,166,10,14,208,2,29,75,35,53,47,72,288,382,204,289,241,48,0,201,218,127,227,55,56,451,19,70,154,149,223,140,1,20,137,91,24,18,81,60,85,145,103,99,225,100,104,105,116,113,111,128,123,172,221,235,198,636,101,102,529,229,97,199,129,132,398,206,209,908,432,126,207,210,211,16,76,45,38,51,21,78,107,147,426,22,52,917,34,73,161,32,33,152,3,214,501,372,294,66,189,190,135,136,200,8,65,171,68,49,148,69,197,138,321,299,119,63,62,4,96,71,122,187,220};
    }

    @Override
    protected MyList<Question> getDataList() {
        return this.questionList;
    }

    @Override
    protected SearchEngine<Question> getSearchEngine() {
        return this.searchEngine;
    }

    @Override
    protected FilterChain<Question> getFilterChain() {
        return this.filterChain;
    }

    @Override
    protected List<MySearchConditionPanel<Question>> getSearchCondition() {
        int[] inter150 = getInter150();
        Arrays.sort(inter150);
        // 添加inter 150过滤器
        this.filterChain.addFilter(new Filter<Question, String>() {
            @Override
            public boolean doFilter(Question question) {
                return contains(question.getFrontendQuestionId());
            }

            @Override
            public Filter<Question, String> addItem(String item) {
                return null;
            }

            @Override
            public boolean contains(String item) {
                int id;
                try {
                    id = Integer.parseInt(item) - 1;
                } catch (Exception ex) {
                    return false;
                }
                return ArrayUtils.binarySearch(inter150, id) != -1;
            }

            @Override
            public boolean removeItem(String item) {
                return false;
            }

            @Override
            public void removeAllItems() {
            }

            @Override
            public int itemCount() {
                return 150;
            }

            @Override
            public boolean usable() {
                return true;
            }
        });
        List<MySearchConditionPanel<Question>> list = new ArrayList<>();
        // 添加算法过滤条件
        /*
        因为hot 100属于固定死的题目, 因此所有过滤内容全部写死
         */
        list.add(new MySearchConditionPanel<>(super::updateText, "算法") {
            @Override
            public OptionConvert createConvert() {
                MapOptionConverter map = new MapOptionConverter(20);
                map.addPair("数组 / 字符串", "87,26,25,79,168,188,120,121,54,44,273,379,237,133,134,41,12,11,57,13,150,5,27,67");
                map.addPair("双指针", "124,391,166,10,14");
                map.addPair("滑动窗口", "208,2,29,75");
                map.addPair("矩阵", "35,53,47,72,288");
                map.addPair("哈希表", "382,204,289,241,48,0,201,218,127");
                map.addPair("区间", "227,55,56,451");
                map.addPair("栈", "19,70,154,149,223");
                map.addPair("链表", "140,1,20,137,91,24,18,81,60,85,145");
                map.addPair("二叉树", "103,99,225,100,104,105,116,113,111,128,123,172,221,235");
                map.addPair("二叉树层次遍历", "198,636,101,102");
                map.addPair("二叉搜索树", "529,229,97");
                map.addPair("图", "199,129,132,398,206,209");
                map.addPair("图的广度优先搜索", "908,432,126");
                map.addPair("字典树", "207,210,211");
                map.addPair("回溯", "16,76,45,38,51,21,78");
                map.addPair("分治", "107,147,426,22");
                map.addPair("Kadane 算法", "52,917");
                map.addPair("二分查找", "34,73,161,32,33,152,3");
                map.addPair("堆", "214,501,372,294");
                map.addPair("位运算", "66,189,190,135,136,200");
                map.addPair("数学", "8,65,171,68,49,148");
                map.addPair("一维动态规划", "69,197,138,321,299");
                map.addPair("多维动态规划", "119,63,62,4,96,71,122,187,220");
                return map;
            }

            @Override
            public Filter<Question, String> createFilter() {
                return new DCAlgorithmFilter();
            }
        });
        // 添加难度过滤条件
        list.add(super.initDifficultyCond());
        // 添加状态过滤条件
        list.add(super.initStatusCond());
        return list;
    }

    @Override
    protected List<Question> getUpdateData() {
        return inter150;
    }

    @Override
    public MyList<Question> getMyList() {
        return this.questionList;
    }

    @Subscribe
    public void rePositionEventListeners(RePositionEvent event) {
        Boolean state = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CODING_STATE);
        // state为true, 正常显示; 否则是deep coding显示模式, 不能在SearchPanel定位
        if (Boolean.TRUE.equals(state)) {
            return;
        }
        String tabName = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CHOOSEN_TAB_NAME);
        // 当前打开的tab是不是 面试 150 题
        if (!INTERVIEW_150_TEXT.equals(tabName)) {
            return;
        }
        // 这里需要清除Interview150Panel设置的搜索条件, 不然查询到的数据是缺失的
        this.clear();

        ViewUtils.rePositionInDeepCoding(event, questionList, project, INTER150);
    }

    @Subscribe
    public void loginEventListener(LoginEvent listener) {
        // 提前调用indexLock(). 因为登录后必定要重新加载所有题目数据, 从而rebuild engine's index
        indexLock();
        questionList.setEmptyText("Loading data, please wait a second...");
    }

    /**
     * 当数据加载完毕后, 执行渲染逻辑
     * @param event event
     */
    @Subscribe
    public void qLoadEndListener(QLoadEndEvent event) {
        unLock();
        questionList.setEmptyText("Noting to show...");
        initMyList();
        updateText();
    }

    @Subscribe
    public void qLoadStartListener(QLoadStartEvent event) {
        indexLock();
        questionList.setEmptyText("Loading data, please wait a second...");
        questionList.setNonData();
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        indexLock();
        questionList.setEmptyText("Loading data, please wait a second...");
        questionList.setNonData();
        initMyList();
        unLock();
        updateText();
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setEmptyText("Please login first...");
        questionList.setNonData();
    }

}
