package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBComboBoxLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.UIUtil;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.listener.QuestionListener;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.render.QuestionCellRender;
import com.xhf.leetcode.plugin.search.engine.QuestionEngine;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.filter.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.yaml.snakeyaml.parser.ParserException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

/**
 * 搜索面板, 提供题目搜索的能力. 内部封装了一个搜索引擎, 提供高效的搜索能力
 */
@LCSubscriber(events = {LoginEvent.class, ClearCacheEvent.class, CodeSubmitEvent.class})
public class SearchPanel extends SimpleToolWindowPanel {
    private final JPanel searchBar;
    private final JTextField searchField;
    private final QuestionEngine engine;
    private final Project project;
    private MyList<Question> questionList;
    // 锁定标志位, lock == true, 当前搜索面板的状态处于锁定状态, 不提供搜索服务
    // lock == false, 解锁
    private boolean lock;
    // 筛选条件组件放置的面板容器
    private JPanel conditionGroup;
    // 封装难度筛选组件的容器面板
    private MySearchConditionPanel difficultyCondPanel;
    // 封装状态筛选组件的容器面板
    private MySearchConditionPanel statusCondPanel;
    // 封装题目分类筛选组件的容器面板
    private MySearchConditionPanel categoryCondPanel;
    // 过滤链
    private final QFilterChain filterChain;

    public SearchPanel(Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.searchBar = new JPanel();
        this.engine = new QuestionEngine(project);
        this.searchField = new JTextField();
        this.project = project;
        this.filterChain = new QFilterChain();
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

        searchBar.add(conditionGroup);
    }

    /**
     * 初始化题目类别筛选条件
     */
    private void initCategoryCond() {
        this.categoryCondPanel = new MySearchConditionPanel(this::updateText, "Category") {
            @Override
            public OptionConvert createConvert() {
                // converted匹配的是question的tag的slug
                ArrayOptionConverter converter = new ArrayOptionConverter(6);
                /**
                 * algorithms
                 * database
                 * shell
                 * concurrency
                 * javascript
                 * pandas
                 */
                converter.addPair("算法", "algorithms");
                converter.addPair("数据库", "database");
                converter.addPair("Shell", "shell");
                converter.addPair("多线程", "concurrency");
                converter.addPair("Javascript", "javascript");
                converter.addPair("Pandas", "pandas");
                return converter;
            }

            @Override
            public QFilter createFilter() {
                return new CategoryFilter();
            }
        };
        doAfterInitCond(this.categoryCondPanel);
    }

    /**
     * 初始化难度筛选条件
     */
    private void initDifficultyCond() {
        difficultyCondPanel = new MySearchConditionPanel(this::updateText, "Difficulty") {
            @Override
            public OptionConvert createConvert() {
                /**
                 * EASY
                 * MEDIUM
                 * HARD
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
        statusCondPanel = new MySearchConditionPanel(this::updateText, "Status") {
            @Override
            public OptionConvert createConvert() {
                /**
                 * AC
                 * TRIED
                 * NOT_STARTED
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
        filterChain.addFilter(conditionPanel.getFilter());
        // 将 ComboBox 添加到面板中
        conditionGroup.add(conditionPanel);
    }

    /**
     * 初始化搜索内容框: 搜索框 + 搜索图标
     */
    private void initSearchContent() {
        lock = true;

        // setLayout(new BorderLayout());
        loginLock();
        // 回车事件, 回车触发后, 执行搜索逻辑
        searchField.addActionListener(e -> {
            updateText();
        });
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
        questionList = new MyList();
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
        searchField.setEnabled(false);
        searchField.setFont(searchField.getFont().deriveFont(Font.ITALIC));
        searchField.setText("please login first");
        searchField.setForeground(Color.RED);
        searchField.repaint(); // 强制重绘
    }


    // 索引锁定, 该方法表示搜索引擎目前正在构建索引, 不提供搜索服务
    public void indexLock() {
        lock = true;
        searchField.setEnabled(false);
        searchField.setFont(searchField.getFont().deriveFont(Font.ITALIC));
        searchField.setText("indexing...");
        searchField.setForeground(Color.RED);
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
    public void loginEventListeners(LoginEvent event) {
        indexLock();
        questionList.setEmptyText("loading data, please wait a second...");
        questionList.setNonData();
        // load or update total question and build index
        QuestionService.getInstance().loadAllQuestionData(project, this.questionList, questions -> {
            buildIndex(questions);
            unLock();
        });
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        indexLock();
        questionList.setEmptyText("loading data, please wait a second...");
        questionList.setNonData();
        // load or update total question and build index
        QuestionService.getInstance().loadAllQuestionData(project, this.questionList, questions -> {
            buildIndex(questions);
            unLock();
        });
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setNonData();
        questionList.setEmptyText("Please login first...");
    }
}