package com.xhf.leetcode.plugin.editors;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.listener.SubmissionListener;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Submission;
import com.xhf.leetcode.plugin.model.SubmissionDetail;
import com.xhf.leetcode.plugin.render.SubmissionCellRender;
import com.xhf.leetcode.plugin.service.LoginService;
import com.xhf.leetcode.plugin.service.SubmissionService;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

/**
 * 订阅login, clearCache, codeSubmit事件
 * login、codeSubmit, 将会触发加载逻辑, 重新获取登录用户的submission数据
 * clearCache逻辑触发, 则会加载openError()方法, 其作用和SolotionEditor中的openError()方法功能一致
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LCSubscriber(events = {LoginEvent.class, ClearCacheEvent.class, CodeSubmitEvent.class})
public class SubmissionEditor extends AbstractSplitTextEditor {

    public SubmissionEditor(Project project, VirtualFile file) {
        super(project, file);
        // register
        LCEventBus.getInstance().register(this);
    }

    @Override
    protected void initFirstComp() {
        if (! LoginService.getInstance(project).isLogin()) {
            JTextPane jTextPane = this.showNotingTextPane();
            jTextPane.setText(Constants.PLEASE_LOGIN);
            jbSplitter.setFirstComponent(jTextPane);
            return;
        }
        MyList<Submission> myList = new MyList<>();
        // make list can show content with multi-line
        myList.setCellRenderer(new SubmissionCellRender());
        // make list can interact with user and open to solution content by click
        myList.addMouseListener(new SubmissionListener(project, myList, this));
        myList.setFont(Constants.CN_FONT);
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);
        if (lc == null) {
            openError();
            return;
        }
        SubmissionService.loadSubmission(project, myList, lc.getTitleSlug());
        myList.setEmptyText(Constants.NOTING_TO_SHOW);
        ApplicationManager.getApplication().invokeLater(() -> {
            jbSplitter.setFirstComponent(new JBScrollPane(myList));
        });
    }

    /**
     * 创建打开错误提示
     */
    private void openError() {
        JTextPane jTextPane = showNotingTextPane();
        jTextPane.setText(Constants.SUBMISSION_EDITOR_OPEN_ERROR);
        BorderLayoutPanel secondComponent = JBUI.Panels.simplePanel(jTextPane);
        jTextPane.setForeground(Constants.RED_COLOR);
        jbSplitter.setFirstComponent(secondComponent);
        jbSplitter.setSecondComponent(null);
    }

    /**
     * submissionEditor通过获取submission_id, 从storeService中获取submission_detail, 并构建codeEditor
     * @param content submission_id
     */
    @Override
    public void openSecond(Map<String, Object> content) {
        // build light virtual file
        if (content == null) {
            LogUtils.error("content is null!");
            openError();
            return;
        }
        if (content.get(Constants.SUBMISSION_ID) == null) {
            LogUtils.error("submission id is null");
            openError();
            return;
        }
        SubmissionDetail submissionDetail = StoreService.getInstance(project).getCache(
                MapUtils.getString(content, Constants.SUBMISSION_ID), SubmissionDetail.class
        );
        if (submissionDetail == null) {
            LogUtils.error("submissionDetail is null");
            openError();
            return;
        }

        // 使用 BorderLayoutPanel 实现上下布局
        BorderLayoutPanel centerPanel = JBUI.Panels.simplePanel();
        // 添加codeEditor
        centerPanel.addToCenter(createCodePanel(submissionDetail));
        // 如果提交结果不是Accepted，则显示错误信息
        boolean flag = submissionDetail.getStatusDisplay().equals("Accepted");
        if (! flag) {
            centerPanel.addToTop(createErrorInfoPanel(submissionDetail));
        }

        BorderLayoutPanel secondComponent = JBUI.Panels.simplePanel(centerPanel);
        secondComponent.addToTop(createToolbarWrapper(centerPanel));
        // 如果有错误提交信息, 添加滚轮
        if (! flag) {
            jbSplitter.setSecondComponent(new JBScrollPane(secondComponent));
        }else {
            jbSplitter.setSecondComponent(secondComponent);
        }
    }

    /**
     * 创建codeEditor
     * @param submissionDetail 提交信息
     * @return CodeEditor
     */
    public JComponent createCodePanel(SubmissionDetail submissionDetail) {
        CodeEditor codeEditor = new CodeEditor(project, submissionDetail.getCode());
        // 设置水平滚轮
        JBScrollPane jsp = new JBScrollPane(codeEditor.getComponent());
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        return jsp;
    }

    /**
     * 创建错误信息editor
     * @param submissionDetail submission的信息
     * @return ErrorInfoEditor
     */
    public JComponent createErrorInfoPanel(SubmissionDetail submissionDetail) {
        ErrorInfoEditor errorInfoEditor = new ErrorInfoEditor(project, submissionDetail);
        // 设置水平滚轮
        JBScrollPane jsp = new JBScrollPane(errorInfoEditor.getComponent());
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        return jsp;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Submission Editor";
    }

    @Subscribe
    public void loginEventLister(LoginEvent event) {
        this.initFirstComp();
    }

    @Subscribe
    public void codeSubmitListener(CodeSubmitEvent event) {
        this.initFirstComp();
    }

    @Subscribe
    public void clearCacheListener(ClearCacheEvent event) {
        openError();
    }
}
