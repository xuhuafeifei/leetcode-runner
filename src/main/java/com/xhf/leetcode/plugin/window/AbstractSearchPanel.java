package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.listener.AbstractMouseAdapter;
import com.xhf.leetcode.plugin.model.DeepCodingInfo;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.render.QuestionCellRender;
import com.xhf.leetcode.plugin.search.engine.SearchEngine;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.filter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象搜索面板, 提供基础的搜索界面显示的能力
 * <p>
 * 该类需要子类提供dataList(数据显示能力)、searchEngine(数据搜索能力)、filterChain(数据过滤能力)
 * 此外, 还需要子类提供筛选组件
 * <p>
 * 需要注意的是, 子类构建父类的时候, 需要调用init()方法进行初始化
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractSearchPanel<T> extends SimpleToolWindowPanel {
    private final JPanel searchBar;
    /**
     * 搜索框, 其中存储用户输入的搜索条件
     */
    private final JTextField searchField;
    protected final Project project;
    // 锁定标志位, lock == true, 当前搜索面板的状态处于锁定状态, 不提供搜索服务
    // lock == false, 解锁
    private boolean lock;
    /**
     * 所有的搜索条件Panel都存储在Array中
     */
    private final List<MySearchConditionPanel<T>> conditionPanelArray;
    // 筛选条件组件放置的面板容器
    private JPanel conditionGroup;

    public AbstractSearchPanel(Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;
        this.searchBar = new JPanel();
        this.searchField = new JTextField();
        this.conditionPanelArray = new ArrayList<>(5);
    }

    protected abstract MyList<T> getDataList();

    protected abstract SearchEngine<T> getSearchEngine();

    protected abstract FilterChain<T> getFilterChain();

    /**
     * 子类必须调用, 否则父类的UI界面无法正常显示
     */
    protected void init() {
        initSearchBar();
    }

    private void initSearchBar() {
        this.add(searchBar, BorderLayout.NORTH);
        this.searchBar.setLayout(new BoxLayout(this.searchBar, BoxLayout.Y_AXIS));

        addToSearchBarBefore(searchBar);
        initSearchContent();
        initSearchCondition();

        this.setToolbar(searchBar);
    }

    /**
     * 允许子类在searchBar的最开始部分添加内容
     * @param searchBar sb
     */
    protected void addToSearchBarBefore(JPanel searchBar) {

    }

    /**
     * 添加搜索筛选条件
     */
    private void initSearchCondition() {
        conditionGroup = new JPanel();

        List<MySearchConditionPanel<T>> searchCondition = getSearchCondition();

        addToConditionGroup(conditionGroup);

        for (MySearchConditionPanel<T> mySearchConditionPanel : searchCondition) {
            doAfterInitCond(mySearchConditionPanel);
        }

        JBScrollPane scrollPane = new JBScrollPane(conditionGroup);
        // 用水平滚动条
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // 禁用垂直滚动条
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        searchBar.add(scrollPane);
    }

    /**
     * 为LC-Competition Panel开的后门
     * @param conditionGroup JPanel
     */
    protected void addToConditionGroup(JPanel conditionGroup) {

    }

    protected abstract List<MySearchConditionPanel<T>> getSearchCondition();

    /**
     * 提供默认的tag condition组件
     * @return condition组件
     */
    protected MySearchConditionPanel<Question> initTagsCond() {
        // converted匹配的时question的tag的slug
        return new MySearchConditionPanel<>(this::updateText, "算法标签") {
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
    }

    /**
     * 提供默认的 初始化题目类别筛选条件
     * @return condition组件
     */
    protected MySearchConditionPanel<Question> initCategoryCond() {
        // converted匹配的是question的tag的slug
        // 封装题目分类筛选组件的容器面板
        return new MySearchConditionPanel<>(this::updateText, "分类") {
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
    }

    /**
     * 提供默认的初始化难度筛选条件
     */
    protected MySearchConditionPanel<Question> initDifficultyCond() {
        // 封装难度筛选组件的容器面板
        return new MySearchConditionPanel<>(this::updateText, "难度") {
            @Override
            public OptionConvert createConvert() {
                /*
                  EASY
                  MEDIUM
                  HARD
                 */
                ArrayOptionConverter converter = new ArrayOptionConverter(3);
                converter.addPair("简单", "EASY");
                converter.addPair("中等", "MEDIUM");
                converter.addPair("困难", "HARD");
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new DifficultyFilter();
            }
        };
    }

    /**
     * 提供默认的status筛选组件
     * @return search condition
     */
    protected MySearchConditionPanel<Question> initStatusCond() {
        /*
          AC
          TRIED
          NOT_STARTED
         */
        // 封装状态筛选组件的容器面板
        return new MySearchConditionPanel<Question>(this::updateText, "题目状态") {
            @Override
            public OptionConvert createConvert() {
                /*
                  AC
                  TRIED
                  NOT_STARTED
                 */
                ArrayOptionConverter converter = new ArrayOptionConverter(3);
//                converter.addPair("solved", "AC");
//                converter.addPair("trying", "TRIED");
//                converter.addPair("todo", "NOT_STARTED");
                converter.addPair("已解决", "AC");
                converter.addPair("尝试中", "TRIED");
                converter.addPair("未开始", "NOT_STARTED");
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new StatusFilter();
            }
        };
    }

    /**
     * 存储并统一管理条件面板, 并将过滤器添加到过滤器链中
     * @param conditionPanel 条件面板
     */
    protected void doAfterInitCond(MySearchConditionPanel<T> conditionPanel) {
        conditionPanel.setEnabled(false);
        getFilterChain().addFilter(conditionPanel.getFilter());
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

    // 登陆锁定. 该方法表示当前项目处于未登录状态, 不提供搜索服务
    public final void loginLock() {
        // 判断当前系统是否登录
        LoginService instance = LoginService.getInstance(project);
        if (instance.isLogin()) {
            instance.doLoginAfter();
        }
        lock = true;
        this.conditionPanelArray.forEach(e -> e.setEnabled(false));
        searchField.setEnabled(false);
        searchField.setFont(searchField.getFont().deriveFont(Font.ITALIC));
        searchField.setText("请先登录");
        searchField.setForeground(JBColor.RED);
        searchField.repaint(); // 强制重绘
    }


    // 索引锁定, 该方法表示搜索引擎目前正在构建索引, 不提供搜索服务
    public final void indexLock() {
        lock = true;
        this.conditionPanelArray.forEach(e -> e.setEnabled(false));
        searchField.setEnabled(false);
        searchField.setFont(searchField.getFont().deriveFont(Font.ITALIC));
        searchField.setText("indexing...");
        searchField.setForeground(JBColor.RED);
        searchField.repaint(); // 强制重绘
    }

    // 释放锁资源, 提供搜索服务. 值得注意的是, 锁(lock)的释放需要在最后执行
    public final void unLock() {
        searchField.setEnabled(true);
        searchField.setText("");
        searchField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        searchField.setFont(UIManager.getFont("TextField.font")); // 设置为默认字体
        searchField.setForeground(UIManager.getColor("TextField.foreground"));
        lock = false;
        this.conditionPanelArray.forEach(e -> e.setEnabled(true));
    }

    /**
     * 获取需要被更新的最完整的原始数据
     * 该方法会在搜索/过滤触发时调用
     * @return data
     */
    protected abstract List<T> getUpdateData();

    // 更新文本
    protected final void updateText() {
        // 如果处于锁定状态, 则直接返回
        if (lock) return;
        String searchText = searchField.getText();
        var filterChain = getFilterChain();
        var dataList = getDataList();
        // 如果是空白内容, 查询全部内容
        if (StringUtils.isBlank(searchText)) {
            List<T> updateData = getUpdateData();
            List<T> apply = filterChain.apply(updateData);
            dataList.setListData(apply);
            return;
        }
        try {
            List<T> res = getSearchEngine().search(searchText);
            List<T> apply = filterChain.apply(res);
            dataList.setListData(apply);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract MyList<T> getMyList();

    protected final void clear() {
        for (MySearchConditionPanel<T> panel : this.conditionPanelArray) {
            panel.clear();
        }
        this.searchField.setText("");
    }

    /**
     * 用于帮助deep coding 初始化MyList使用的函数
     * @param questionList questionList
     * @param questions questions
     * @param pattern pattern
     */
    protected void initMyListHelper(MyList<Question> questionList, List<Question> questions, String pattern) {
        questionList.setListData(questions);
        questionList.setCellRenderer(new QuestionCellRender());
        questionList.addMouseListener(new AbstractMouseAdapter(project) {
            @Override
            protected void doubleClicked(MouseEvent e) {
                Point point = e.getPoint();
                int idx = questionList.locationToIndex(point);
                DeepCodingInfo dci = new DeepCodingInfo(pattern, questionList.getModel().getSize(), idx);
                Question question = questionList.getModel().getElementAt(idx);
                CodeService.getInstance(project).openCodeEditor(question, dci);
            }
        });

        JBScrollPane jbScrollPane = new JBScrollPane(questionList);
        this.add(jbScrollPane, BorderLayout.CENTER);
        this.setContent(jbScrollPane);
    }


    /**
     * 检查当前是否处于deep coding模式, 并且打开的tab的名称是不是checkTabName
     * @param checkTabName tab 名称
     * @return boolean
     */
    protected boolean doCheck(String checkTabName) {
        AtomicReference<Boolean> state = new AtomicReference<>();
        Application app = ApplicationManager.getApplication();
        app.invokeAndWait(() -> {
            state.set(LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CODING_STATE));
        });
        // state为true, 正常显示; 否则是deep coding显示模式, 不能在SearchPanel定位
        if (Boolean.TRUE.equals(state.get())) {
            return false;
        }
        AtomicReference<String> tabName = new AtomicReference<>();
        app.invokeAndWait(() -> {
            tabName.set(LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CHOOSEN_TAB_NAME));
        });

        return checkTabName.equals(tabName.get());
    }
}
