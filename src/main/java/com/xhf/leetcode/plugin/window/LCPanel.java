package com.xhf.leetcode.plugin.window;

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

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCPanel extends SimpleToolWindowPanel implements DataProvider, LCSubscriber {
    private MyList<Question> questionList;

    private Project project;

    public LCPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();

        // register
        registerToLCEventBus();

        // get action toolbar
        ActionToolbar actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.plugin.lcActionsToolbar"),
                true);

        initLeetcodeClient();
        initMyList();


        // search panel
        SearchPanel searchPanel = new SearchPanel();
        searchPanel.add(new JBScrollPane(questionList), BorderLayout.CENTER);

        // store to action toolbar
        actionToolbar.setTargetComponent(questionList);
        setToolbar(actionToolbar.getComponent());
        setContent(searchPanel);
    }

    /**
     * register this class to EventBus
     * if the event is fired, this class will be notified and reload or update MyList
     *
     */
    private void registerToLCEventBus() {
        LCEventBus.getInstance().register(LoginEvent.class, this);
        LCEventBus.getInstance().register(CodeSubmitEvent.class, this);
    }

    private void initMyList() {
        // build question list
        questionList = new MyList();
        questionList.setCellRenderer(new QuestionCellRender());
        questionList.addMouseListener(new QuestionListener(questionList, project));
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

    @Override
    public void onEvent(LCEvent event) {
        // load or update total question
        QuestionService.getInstance().loadAllQuestionData(project, this.questionList);
    }

    private class SearchPanel extends SimpleToolWindowPanel {
        private JTextField searchField;
        private QuestionEngine engine;

        public SearchPanel() {
            super(Boolean.TRUE, Boolean.TRUE);
            init();
        }

        private void init() {
            engine = new QuestionEngine(project);
            try {
                engine.buildIndex(QuestionService.getInstance().getTotalQuestion(project));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            setLayout(new BorderLayout());
            searchField = new JTextField();
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

        private void updateText() {
            String searchText = searchField.getText();
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
