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
 * leetcode 经典热题 100道
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Hot100Panel extends AbstractSearchPanel<Question> {
    /**
     * 用于DeepCodingInfo 存储的信息, 表示当前通过deep coding哪个模式创建的文件
     */
    public static final String HOT100 = "hot100";
    /**
     * 用于 panel tab显示的信息
     */
    public static final String HOT_100_TEXT = "Hot 100 题";
    private final QFilterChain filterChain;
    private final QuestionEngine engine;
    private final Project project;
    private final MyList<Question> questionList;
    // hot 100的题目
    private List<Question> hot100;

    public Hot100Panel(Project project) {
        super(project);
        this.engine = QuestionEngine.getInstance(project);
        this.project = project;
        this.questionList = new MyList<>();
        initMyList();
        this.filterChain = new QFilterChain();
        LCEventBus.getInstance().register(this);
        super.init();
        // 一定要unLock. 因为deepCoding功能一定是登陆后才开放, 无需进行锁定
//        super.unLock();
    }

    private void initMyList() {
        List<Question> totalQuestion = QuestionService.getInstance().getTotalQuestion(project);
        int[] hot100Id = getHot100();
        this.hot100 = new ArrayList<>(120);
        for (int idx : hot100Id) {
            hot100.add(totalQuestion.get(idx));
        }

        super.initMyListHelper(questionList, hot100, HOT100);
    }

    private int[] getHot100() {
        // 缓存100道热题的id
        return new int[]{0,48,127,282,10,14,41,2,437,559,238,75,52,55,188,237,40,72,53,47,239,159,205,233,140,141,20,1,18,23,24,137,147,22,145,93,103,225,100,542,101,107,97,229,198,113,104,436,235,123,199,993,206,207,45,77,16,38,21,78,130,50,34,73,33,32,152,3,19,154,393,738,83,214,346,294,120,54,44,762,69,117,197,278,321,138,299,151,415,31,61,63,4,1142,71,135,168,74,30,286};
    }

    @Override
    protected MyList<Question> getDataList() {
        return this.questionList;
    }

    @Override
    protected SearchEngine<Question> getSearchEngine() {
        return this.engine;
    }

    @Override
    protected FilterChain<Question> getFilterChain() {
        return this.filterChain;
    }

    @Override
    protected List<MySearchConditionPanel<Question>> getSearchCondition() {
        int[] hot100Id = getHot100();
        Arrays.sort(hot100Id);
        // 添加hot 100过滤器
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
                // hot100Id存储的是下标, fid需要-1才能成为下标
                return ArrayUtils.binarySearch(hot100Id, id) != -1;
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
                return 100;
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
                map.addPair("哈希", "1,49,128");
                map.addPair("双指针", "283,11,15,42");
                map.addPair("滑动窗口", "3,438");
                map.addPair("子串", "560,239,76");
                map.addPair("普通数组", "53,56,189,238,41");
                map.addPair("矩阵", "73,54,48,240");
                map.addPair("链表", "160,206,234,141,142,21,2,19,24,25,138,148,23,146");
                map.addPair("二叉树", "94,104,226,101,543,102,108,98,230,199,114,105,437,236,124");
                map.addPair("图论", "200,994,207,208");
                map.addPair("回溯", "46,78,17,39,22,79,131,51");
                map.addPair("二分查找", "35,74,34,33,153,4");
                map.addPair("栈", "20,155,394,739,84");
                map.addPair("堆", "215,347,295");
                map.addPair("贪心算法", "121,55,45,763");
                map.addPair("动态规划", "70,118,198,279,322,139,300,152,416,32");
                map.addPair("多维动态规划", "62,64,5,1143,72");
                map.addPair("技巧", "136,169,75,31,287");
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
        if (this.hot100 == null || this.hot100.isEmpty()) {
            initMyList();
        }
        return this.hot100;
    }

    @Override
    public MyList<Question> getMyList() {
        return this.questionList;
    }

    public List<Question> getHot100Data() {
        return hot100;
    }

    @Subscribe
    public void rePositionEventListeners(RePositionEvent event) {
        if (! super.doCheck(HOT_100_TEXT)) {
            return;
        }
        // 这里需要清除Hot100Panel设置的搜索条件, 不然查询到的数据是缺失的
        this.clear();

        ViewUtils.rePositionInDeepCoding(event, questionList, project, HOT100);
    }

    @Subscribe
    public void loginEventListener(LoginEvent listener) {
        // 提前调用indexLock(). 因为登录后必定要重新加载所有题目数据, 从而rebuild engine's index
        indexLock();
        questionList.setEmptyText("数据加载中, 请稍等");
    }

    /**
     * 当数据加载完毕后, 执行渲染逻辑
     * @param event event
     */
    @Subscribe
    public void qLoadEndListener(QLoadEndEvent event) {
        unLock();
        questionList.setEmptyText("没有可用于展示的数据...");
        initMyList();
        updateText();
    }

    @Subscribe
    public void qLoadStartListener(QLoadStartEvent event) {
        indexLock();
        questionList.setEmptyText("数据加载中, 请稍等");
        questionList.setNonData();
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        if (! super.doCheck(HOT_100_TEXT)) {
            return;
        }
        indexLock();
        questionList.setEmptyText("数据加载中, 请稍等");
        questionList.setNonData();
        initMyList();
        unLock();
        updateText();
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setEmptyText("请先登录...");
        questionList.setNonData();
    }
}
