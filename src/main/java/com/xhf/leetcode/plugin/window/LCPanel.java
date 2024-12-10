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
public class LCPanel extends SimpleToolWindowPanel implements DataProvider {
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

        // search panel
        searchPanel = new SearchPanel(project);

        // store to action toolbar
        actionToolbar.setTargetComponent(searchPanel.getMyList());
        setToolbar(actionToolbar.getComponent());
        setContent(searchPanel);

        // register
        LCEventBus.getInstance().register(this);
    }

    private void initLeetcodeClient() {
        LeetcodeClient.init(this.project);
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_QUESTION_LIST.is(dataId)) {
            return searchPanel.getMyList();
        }
        return null;
    }
}
