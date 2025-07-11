package com.xhf.leetcode.plugin.window.deepcoding;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.CodeSubmitEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LoginEvent;
import com.xhf.leetcode.plugin.bus.QLoadEndEvent;
import com.xhf.leetcode.plugin.bus.QLoadStartEvent;
import com.xhf.leetcode.plugin.bus.RePositionEvent;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.search.engine.QuestionEngine;
import com.xhf.leetcode.plugin.search.engine.SearchEngine;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.ArrayUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.TaskCenter;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.AbstractSearchPanel;
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
    public static final String INTERVIEW_150_TEXT = BundleUtils.i18nHelper("经典面试 150 题", "Classic Interview 150");
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
        if (LoginService.getInstance(project).isLogin()) {
            super.unLock();
        }
    }

    private void initMyList() {
        TaskCenter.getInstance().createTask(() -> {
            List<Question> totalQuestion = QuestionService.getInstance(project).getTotalQuestion(project);
            int[] inter150Id = getInter150();
            this.inter150 = new ArrayList<>(180);
            for (int idx : inter150Id) {
                inter150.add(totalQuestion.get(idx));
            }

            super.initMyListHelper(this.questionList, this.inter150, INTER150);
        }).invokeLater();
    }

    private int[] getInter150() {
        // 缓存面试150题的下标
        return new int[]{87, 26, 25, 79, 168, 188, 120, 121, 54, 44, 273, 379, 237, 133, 134, 41, 12, 11, 57, 13, 150,
            5, 27, 67, 124, 391, 166, 10, 14, 208, 2, 29, 75, 35, 53, 47, 72, 288, 382, 204, 289, 241, 48, 0, 201, 218,
            127, 227, 55, 56, 451, 19, 70, 154, 149, 223, 140, 1, 20, 137, 91, 24, 18, 81, 60, 85, 145, 103, 99, 225,
            100, 104, 105, 116, 113, 111, 128, 123, 172, 221, 235, 198, 636, 101, 102, 529, 229, 97, 199, 129, 132, 398,
            206, 209, 908, 432, 126, 207, 210, 211, 16, 76, 45, 38, 51, 21, 78, 107, 147, 426, 22, 52, 917, 34, 73, 161,
            32, 33, 152, 3, 214, 501, 372, 294, 66, 189, 190, 135, 136, 200, 8, 65, 171, 68, 49, 148, 69, 197, 138, 321,
            299, 119, 63, 62, 4, 96, 71, 122, 187, 220};
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
        list.add(new MySearchConditionPanel<>(super::updateText, BundleUtils.i18nHelper("算法", "algorithm")) {
            @Override
            public OptionConvert createConvert() {
                MapOptionConverter map = new MapOptionConverter(20);
                if (AppSettings.getInstance().isZh()) {
                    map.addPair("数组 / 字符串",
                        "88,27,26,80,169,189,121,122,55,45,274,380,238,134,135,42,13,12,58,14,151,6,28,68");
                    map.addPair("双指针", "125,392,167,11,15");
                    map.addPair("滑动窗口", "209,3,30,76");
                    map.addPair("矩阵", "36,54,48,73,289");
                    map.addPair("哈希表", "383,205,290,242,49,1,202,219,128");
                    map.addPair("区间", "228,56,57,452");
                    map.addPair("栈", "20,71,155,150,224");
                    map.addPair("链表", "141,2,21,138,92,25,19,82,61,86,146");
                    map.addPair("二叉树", "104,100,226,101,105,106,117,114,112,129,124,173,222,236");
                    map.addPair("二叉树层次遍历", "199,637,102,103");
                    map.addPair("二叉搜索树", "530,230,98");
                    map.addPair("图", "200,130,133,399,207,210");
                    map.addPair("图的广度优先搜索", "909,433,127");
                    map.addPair("字典树", "208,211,212");
                    map.addPair("回溯", "17,77,46,39,52,22,79");
                    map.addPair("分治", "108,148,427,23");
                    map.addPair("Kadane 算法", "53,918");
                    map.addPair("二分查找", "35,74,162,33,34,153,4");
                    map.addPair("堆", "215,502,373,295");
                    map.addPair("位运算", "67,190,191,136,137,201");
                    map.addPair("数学", "9,66,172,69,50,149");
                    map.addPair("一维动态规划", "70,198,139,322,300");
                    map.addPair("多维动态规划", "120,64,63,5,97,72,123,188,221");
                } else {
                    map.addPair("Array / String",
                        "88,27,26,80,169,189,121,122,55,45,274,380,238,134,135,42,13,12,58,14,151,6,28,68");
                    map.addPair("Two Pointers", "125,392,167,11,15");
                    map.addPair("Sliding Window", "209,3,30,76");
                    map.addPair("Matrix", "36,54,48,73,289");
                    map.addPair("Hash Table", "383,205,290,242,49,1,202,219,128");
                    map.addPair("Interval", "228,56,57,452");
                    map.addPair("Stack", "20,71,155,150,224");
                    map.addPair("Linked List", "141,2,21,138,92,25,19,82,61,86,146");
                    map.addPair("Binary Tree", "104,100,226,101,105,106,117,114,112,129,124,173,222,236");
                    map.addPair("Binary Tree Level Order Traversal", "199,637,102,103");
                    map.addPair("Binary Search Tree", "530,230,98");
                    map.addPair("Graph", "200,130,133,399,207,210");
                    map.addPair("Graph BFS", "909,433,127");
                    map.addPair("Trie", "208,211,212");
                    map.addPair("Backtracking", "17,77,46,39,52,22,79");
                    map.addPair("Divide and Conquer", "108,148,427,23");
                    map.addPair("Kadane's Algorithm", "53,918");
                    map.addPair("Binary Search", "35,74,162,33,34,153,4");
                    map.addPair("Heap", "215,502,373,295");
                    map.addPair("Bit Manipulation", "67,190,191,136,137,201");
                    map.addPair("Math", "9,66,172,69,50,149");
                    map.addPair("1D Dynamic Programming", "70,198,139,322,300");
                    map.addPair("Multi-dimensional Dynamic Programming", "120,64,63,5,97,72,123,188,221");
                }
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
        if (!super.doCheck(INTERVIEW_150_TEXT)) {
            if (!super.doCheck(INTERVIEW_150_TEXT)) {
                return;
            }
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
        questionList.setEmptyText(BundleUtils.i18nHelper("数据加载中, 请稍等", "Loading data, please wait..."));
    }

    /**
     * 当数据加载完毕后, 执行渲染逻辑
     *
     * @param event event
     */
    @Subscribe
    public void qLoadEndListener(QLoadEndEvent event) {
        unLock();
        questionList.setEmptyText(BundleUtils.i18nHelper("没有可用于展示的数据...", "No data available..."));
        initMyList();
        updateText();
    }

    @Subscribe
    public void qLoadStartListener(QLoadStartEvent event) {
        indexLock();
        questionList.setEmptyText(BundleUtils.i18nHelper("数据加载中, 请稍等", "Loading data, please wait..."));
        questionList.setNonData();
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        if (!super.doCheck(INTERVIEW_150_TEXT)) {
            return;
        }
        indexLock();
        questionList.setEmptyText(BundleUtils.i18nHelper("数据加载中, 请稍等", "Loading data, please wait..."));
        questionList.setNonData();
        initMyList();
        unLock();
        updateText();
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setEmptyText(BundleUtils.i18n("action.leetcode.actions.login.info"));
        questionList.setNonData();
    }

}
