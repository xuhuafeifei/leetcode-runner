package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.editors.MarkDownEditor;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.LocalResourceHttpServer;
import com.xhf.leetcode.plugin.listener.QuestionListener;
import com.xhf.leetcode.plugin.render.QuestionCellRender;
import com.xhf.leetcode.plugin.search.engine.QuestionEngine;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LCSubscriber(events = {LoginEvent.class, ClearCacheEvent.class, CodeSubmitEvent.class})
public class LCPanel extends SimpleToolWindowPanel implements DataProvider {
    private MyList<Question> questionList;

    private Project project;
    private SearchPanel searchPanel;

    public LCPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();

        // get action toolbar
        ActionToolbar actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.plugin.lcActionsToolbar"),
                true);

        initLeetcodeClient();
        initMyList();

        // search panel
        searchPanel = new SearchPanel();
        searchPanel.add(new JBScrollPane(questionList), BorderLayout.CENTER);

        // store to action toolbar
        actionToolbar.setTargetComponent(questionList);
        setToolbar(actionToolbar.getComponent());
        setContent(searchPanel);

        // register
        LCEventBus.getInstance().register(this);

        LogUtils.info("demo test...");
    }

    private void initMyList() {
        // build question list
        questionList = new MyList();
        questionList.setCellRenderer(new QuestionCellRender());
        questionList.addMouseListener(new QuestionListener(questionList, project));
        questionList.setEmptyText("Please login first...");
    }

    private void initLeetcodeClient() {
        LeetcodeClient.init(this.project);
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_QUESTION_LIST.is(dataId)) {
            return questionList;
        }
        return super.getData(dataId);
    }

    @Subscribe
    public void loginEventListeners(LoginEvent event) {
        searchPanel.indexLock();
        questionList.setEmptyText("loading data, please wait a second...");
        questionList.setNonData();
        // load or update total question and build index
        QuestionService.getInstance().loadAllQuestionData(project, this.questionList, questions -> {
            searchPanel.buildIndex(questions);
            searchPanel.unLock();
        });
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        searchPanel.indexLock();
        questionList.setEmptyText("loading data, please wait a second...");
        questionList.setNonData();
        // load or update total question and build index
        QuestionService.getInstance().loadAllQuestionData(project, this.questionList, questions -> {
            searchPanel.buildIndex(questions);
            searchPanel.unLock();
        });
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        searchPanel.loginLock();
        questionList.setNonData();
        questionList.setEmptyText("Please login first...");
    }

    /**
     * 搜索面板, 提供题目搜索的能力. 内部封装了一个搜索引擎, 提供高效的搜索能力
     */
    private class SearchPanel extends SimpleToolWindowPanel {
        private final JTextField searchField;
        private final QuestionEngine engine;
        // 锁定标志位, lock == true, 当前搜索面板的状态处于锁定状态, 不提供搜索服务
        // lock == false, 解锁
        private boolean lock;

        public SearchPanel() {
            super(Boolean.TRUE, Boolean.TRUE);
            engine = new QuestionEngine(project);
            searchField = new JTextField();
            init();
        }

        private void init() {
            lock = true;

            setLayout(new BorderLayout());
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

            add(iconPanel, BorderLayout.NORTH);
        }

        // 为搜索引擎提供数据源, 同时创建索引
        public void buildIndex(List<Question> source) {
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
                QuestionService.getInstance().loadAllQuestionData(project, questionList);
                return;
            }
            try {
                List<Question> res = engine.search(searchText);
                questionList.setListData(res);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
