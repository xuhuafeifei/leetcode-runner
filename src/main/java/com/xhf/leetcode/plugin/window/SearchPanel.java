package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.listener.QuestionListener;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.render.QuestionCellRender;
import com.xhf.leetcode.plugin.search.engine.QuestionEngine;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.window.filter.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索面板, 提供题目搜索的能力. 内部封装了一个搜索引擎, 提供高效的搜索能力
 */
@LCSubscriber(events = {LoginEvent.class, ClearCacheEvent.class, CodeSubmitEvent.class, QLoadStartEvent.class, QLoadEndEvent.class})
public class SearchPanel extends SimpleToolWindowPanel {
    private final JPanel searchBar;
    private final JTextField searchField;
    private final QuestionEngine engine;
    private final Project project;
    private MyList<Question> questionList;
    // 锁定标志位, lock == true, 当前搜索面板的状态处于锁定状态, 不提供搜索服务
    // lock == false, 解锁
    private boolean lock;
    private final List<MySearchConditionPanel> conditionPanelArray;
    // 筛选条件组件放置的面板容器
    private JPanel conditionGroup;
    // 过滤链
    private final QFilterChain filterChain;

    public SearchPanel(Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.searchBar = new JPanel();
        this.engine = new QuestionEngine(project);
        this.searchField = new JTextField();
        this.project = project;
        this.filterChain = new QFilterChain();
        this.conditionPanelArray = new ArrayList<>(5);
        LCEventBus.getInstance().register(this);
        init();

    }

    private void init() {
        initSearchBar();
        initMyList();
    }

    private void initSearchBar() {
        this.add(searchBar, BorderLayout.NORTH);
        this.searchBar.setLayout(new BoxLayout(this.searchBar, BoxLayout.Y_AXIS));

        initSearchContent();
        initSearchCondition();

        this.setToolbar(searchBar);
    }

    /**
     * 添加搜索筛选条件
     */
    private void initSearchCondition() {
        conditionGroup = new JPanel();

        initDifficultyCond();
        initStatusCond();
        initCategoryCond();
        initTagsCond();

        JBScrollPane scrollPane = new JBScrollPane(conditionGroup);
        // 用水平滚动条
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // 禁用垂直滚动条
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        searchBar.add(scrollPane);
    }

    private void initTagsCond() {
        // converted匹配的时question的tag的slug
        MySearchConditionPanel tagsCondPanel = new MySearchConditionPanel(this::updateText, "Tags") {
            @Override
            public OptionConvert createConvert() {
                // converted匹配的是question的tag的slug
                var converter = new MapOptionConverter(70);
                String[] options = new String[]{"数组","字符串","排序","矩阵","模拟","枚举","字符串匹配","计数排序","桶排序","基数排序","动态规划","贪心","深度优先搜索","二分查找","广度优先搜索","回溯","递归","分治","记忆化搜索","归并排序","快速选择","哈希表","树","二叉树","堆（优先队列）","栈","图","链表","单调栈","有序集合","队列","二叉搜索树","拓扑排序","最短路","单调队列","双向链表","最小生成树","强连通分量","欧拉回路","双连通分量","并查集","字典树","线段树","树状数组","后缀数组","位运算","双指针","前缀和","计数","滑动窗口","状态压缩","哈希函数","滚动哈希","扫描线","数学","数论","几何","组合数学","博弈","随机化","概率与统计","水塘抽样","拒绝采样","数据库","设计","数据流","交互","脑筋急转弯","迭代器","多线程","Shell"};
                String[] converted = new String[]{"array","string","sorting","matrix","simulation","enumeration","string-matching","counting-sort","bucket-sort","radix-sort","dynamic-programming","greedy","depth-first-search","binary-search","breadth-first-search","backtracking","recursion","divide-and-conquer","memoization","merge-sort","quickselect","hash-table","tree","binary-tree","heap-priority-queue","stack","graph","linked-list","monotonic-stack","ordered-set","queue","binary-search-tree","topological-sort","shortest-path","monotonic-queue","doubly-linked-list","minimum-spanning-tree","strongly-connected-component","eulerian-circuit","biconnected-component","union-find","trie","segment-tree","binary-indexed-tree","suffix-array","bit-manipulation","two-pointers","prefix-sum","counting","sliding-window","bitmask","hash-function","rolling-hash","line-sweep","math","number-theory","geometry","combinatorics","game-theory","randomized","probability-and-statistics","reservoir-sampling","rejection-sampling","database","design","data-stream","interactive","brainteaser","iterator","concurrency","shell"};
                int len = options.length;
                for (int i = 0; i < len; ++i) {
                    converter.addPair(options[i], converted[i]);
                }
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new TagsFilter();
            }
        };
        doAfterInitCond(tagsCondPanel);
    }

    /**
     * 初始化题目类别筛选条件
     */
    private void initCategoryCond() {
        // converted匹配的是question的tag的slug
        // 封装题目分类筛选组件的容器面板
        MySearchConditionPanel categoryCondPanel = new MySearchConditionPanel(this::updateText, "Category") {
            @Override
            public OptionConvert createConvert() {
                // converted匹配的是question的tag的slug
                ArrayOptionConverter converter = new ArrayOptionConverter(6);
                /*
                  algorithms
                  database
                  shell
                  concurrency
                  javascript
                  pandas
                 */
                converter.addPair("算法", "algorithms");
                converter.addPair("数据库", "database");
                converter.addPair("Shell", "shell");
                converter.addPair("多线程", "concurrency");
                converter.addPair("Javascript", "javascript");
                converter.addPair("Pandas", "database");
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new CategoryFilter();
            }
        };
        doAfterInitCond(categoryCondPanel);
    }

    /**
     * 初始化难度筛选条件
     */
    private void initDifficultyCond() {
        // 封装难度筛选组件的容器面板
        MySearchConditionPanel difficultyCondPanel = new MySearchConditionPanel(this::updateText, "Difficulty") {
            @Override
            public OptionConvert createConvert() {
                /*
                  EASY
                  MEDIUM
                  HARD
                 */
                ArrayOptionConverter converter = new ArrayOptionConverter(3);
                converter.addPair("easy", "EASY");
                converter.addPair("medium", "MEDIUM");
                converter.addPair("hard", "HARD");
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new DifficultyFilter();
            }
        };
        doAfterInitCond(difficultyCondPanel);
    }
    private void initStatusCond() {
        /*
          AC
          TRIED
          NOT_STARTED
         */
        // 封装状态筛选组件的容器面板
        MySearchConditionPanel statusCondPanel = new MySearchConditionPanel(this::updateText, "Status") {
            @Override
            public OptionConvert createConvert() {
                /*
                  AC
                  TRIED
                  NOT_STARTED
                 */
                ArrayOptionConverter converter = new ArrayOptionConverter(3);
                converter.addPair("solved", "AC");
                converter.addPair("trying", "TRIED");
                converter.addPair("todo", "NOT_STARTED");
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new StatusFilter();
            }
        };
        doAfterInitCond(statusCondPanel);
    }

    private void doAfterInitCond(MySearchConditionPanel conditionPanel) {
        conditionPanel.setEnabled(false);
        filterChain.addFilter(conditionPanel.getFilter());
        // 将 ComboBox 添加到面板中
        conditionGroup.add(conditionPanel);
        // 存储conditionPanel
        this.conditionPanelArray.add(conditionPanel);
    }

    /**
     * 初始化搜索内容框: 搜索框 + 搜索图标
     */
    private void initSearchContent() {
        lock = true;

        // setLayout(new BorderLayout());
        loginLock();
        // 回车事件, 回车触发后, 执行搜索逻辑
        searchField.addActionListener(e -> updateText());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateText();
            }
        });

        JLabel searchIconLabel = new JLabel(IconLoader.getIcon("/icons/find.svg", SearchPanel.class));
        searchIconLabel.setOpaque(false);

        // mix icon and search text
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.add(searchIconLabel, BorderLayout.WEST);
        iconPanel.add(searchField, BorderLayout.CENTER);

        searchBar.add(iconPanel);
    }

    private void initMyList() {
        // build question list
        questionList = new MyList<>();
        questionList.setCellRenderer(new QuestionCellRender());
        questionList.addMouseListener(new QuestionListener(questionList, project));
        questionList.setEmptyText("Please login first...");

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

    // 登陆锁定. 该方法表示当前项目处于未登录状态, 不提供搜索服务
    public void loginLock() {
        lock = true;
        this.conditionPanelArray.forEach(e -> e.setEnabled(false));
        searchField.setEnabled(false);
        searchField.setFont(searchField.getFont().deriveFont(Font.ITALIC));
        searchField.setText("please login first");
        searchField.setForeground(JBColor.RED);
        searchField.repaint(); // 强制重绘
    }


    // 索引锁定, 该方法表示搜索引擎目前正在构建索引, 不提供搜索服务
    public void indexLock() {
        lock = true;
        this.conditionPanelArray.forEach(e -> e.setEnabled(false));
        searchField.setEnabled(false);
        searchField.setFont(searchField.getFont().deriveFont(Font.ITALIC));
        searchField.setText("indexing...");
        searchField.setForeground(JBColor.RED);
        searchField.repaint(); // 强制重绘
    }

    // 释放锁资源, 提供搜索服务. 值得注意的是, 锁(lock)的释放需要在最后执行
    public void unLock() {
        searchField.setEnabled(true);
        searchField.setText("");
        searchField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        searchField.setFont(UIManager.getFont("TextField.font")); // 设置为默认字体
        searchField.setForeground(UIManager.getColor("TextField.foreground"));
        lock = false;
        this.conditionPanelArray.forEach(e -> e.setEnabled(true));
    }

    // 更新文本
    private void updateText() {
        // 如果处于锁定状态, 则直接返回
        if (lock) return;
        String searchText = searchField.getText();
        // 如果是空白内容, 查询全部内容
        if (StringUtils.isBlank(searchText)) {
            List<Question> totalQuestion = QuestionService.getInstance().getTotalQuestion(project);
            filterChain.apply(totalQuestion, questionList::setListData);
            return;
        }
        try {
            List<Question> res = engine.search(searchText);
            filterChain.apply(res, questionList::setListData);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public MyList<Question> getMyList() {
        return questionList;
    }

    @Subscribe
    public void loginEventListener(LoginEvent listener) {
        // 提前调用indexLock(). 因为登录后必定要重新加载所有题目数据, 从而rebuild engine's index
        indexLock();
        questionList.setEmptyText("Loading data, please wait a second...");
    }

    @Subscribe
    public void qLoadStartListener(QLoadStartEvent event) {
        indexLock();
        questionList.setEmptyText("Loading data, please wait a second...");
        questionList.setNonData();
    }

    /**
     * 当数据加载完毕后, 执行渲染逻辑
     * @param event
     */
    @Subscribe
    public void qLoadEndListener(QLoadEndEvent event) {
        unLock();
        questionList.setEmptyText("Noting to show...");
        List<Question> totalQuestion = QuestionService.getInstance().getTotalQuestion(project);
        filterChain.apply(totalQuestion, questionList::setListData);
        buildIndex(totalQuestion);
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        indexLock();
        questionList.setEmptyText("Loading data, please wait a second...");
        questionList.setNonData();
        buildIndex(QuestionService.getInstance().getTotalQuestion(project));
        unLock();
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setEmptyText("Please login first...");
        questionList.setNonData();
    }
}