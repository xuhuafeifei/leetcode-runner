package com.xhf.leetcode.plugin.editors.text;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.xhf.leetcode.plugin.actions.AbstractAction;
import com.xhf.leetcode.plugin.bus.DeepCodingTabChooseEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.editors.myeditor.MySplitEditorToolbar;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.service.RQServiceImpl;
import com.xhf.leetcode.plugin.review.utils.AbstractMasteryDialog;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import com.xhf.leetcode.plugin.window.deepcoding.Hot100Panel;
import com.xhf.leetcode.plugin.window.deepcoding.Interview150Panel;
import com.xhf.leetcode.plugin.window.deepcoding.LCCompetitionPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class CustomTextEditor implements TextEditor {
    private final Editor editor;
    private final JPanel component;
    private final VirtualFile file;
    private final Project project;
    private final RQServiceImpl service;
    private @Nullable ActionGroup actionGroup;

    public CustomTextEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = file;
        this.project = project;
        this.service = new RQServiceImpl(project);
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            throw new IllegalStateException("Cannot create editor for file: " + file.getName());
        }

        editor = EditorFactory.getInstance().createEditor(document, project, file.getFileType(), false);

        EditorHighlighter highlighter = EditorHighlighterFactory.getInstance()
                .createEditorHighlighter(project, file);
        ((EditorEx) editor).setHighlighter(highlighter);

        component = new JPanel(new BorderLayout());
        component.add(editor.getComponent(), BorderLayout.CENTER);
        component.add(createButtonToolbar(), BorderLayout.SOUTH);
    }

    private JComponent createButtonToolbar() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton rate = new JButton(BundleUtils.i18nHelper("评分", "Rate"));
        rate.addActionListener(e -> {
            // 评分
            showMasteryDialog();
        });
        jPanel.add(rate);
        return jPanel;
    }

    /**
     * 点击掌握按钮, 弹出掌握程度dialog
     * 在dialog中点击确认按钮, 则会同步刷新card数据, 并标记为已完成
     * 同时会向后台发出请求, 更新数据
     */
    private void showMasteryDialog() {
        new AbstractMasteryDialog(this.getComponent(), BundleUtils.i18nHelper("设置掌握程度", "set mastery level")) {

            @Override
            protected void setConfirmButtonListener(JButton confirmButton, ButtonGroup group) {
                confirmButton.addActionListener(e -> {
                    // 获取选中的掌握程度
                    String levelStr = group.getSelection().getActionCommand();

                    service.createQuestion(ViewUtils.getQuestionByVFile(file, project), FSRSRating.getById(levelStr));

                    // 关闭对话框
                    this.dispose();
                });
            }
        };
    }

    public void setToolbar() {
        component.add(createToolbar(), BorderLayout.NORTH);
    }

    public void setToolbar(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
        setToolbar();
    }

    private JComponent createToolbar() {
        ActionGroup action = (ActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.text.group");
        DefaultActionGroup dag = new DefaultActionGroup();
        // 增加语言图标
        dag.add(ViewUtils.createLangIcon(project));
        dag.addSeparator();
        dag.add(action);

        try {
            dag.addSeparator();
            // 判断是否是通过deep coding模式创建
            LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(file, project);
            if (lc != null && lc.getDeepCodingInfo() != null) {
                dag.addSeparator();
                dag.add(createIcon(lc.getDeepCodingInfo().getPattern()));
                dag.add(createPreAction());
                dag.add(createNextAction());
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("CustomTextEditor", dag, true);
        toolbar.setTargetComponent(editor.getComponent());
        return new MySplitEditorToolbar(toolbar, createOrNot());
    }

    private ActionToolbar createOrNot() {
        boolean flag;
        try {
            flag = AppSettings.getInstance().getEnableFloatingToolbar();
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            flag = false;
        }
        // 兼容悬浮窗显示+非悬浮窗显示
        // 如果flag = false, 不显示悬浮toolbar, 在CustomTextEditor显示
        if (!flag) {
            ActionGroup action = (ActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.editor.basic.group");
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("custom_text_editor_toolbar",
                // 如果actionGroup为空, 则不讲他添加到UI
                actionGroup == null ?
                    action : new DefaultActionGroup(action, Separator.create(), actionGroup),
                true);
            actionToolbar.setTargetComponent(editor.getComponent());
            return actionToolbar;
        }
        // 返回一个空的toolbar, 用于占位
        ActionToolbar actionToolbar = ActionManager.getInstance()
            .createActionToolbar("custom_text_empty_editor_toolbar", new DefaultActionGroup(), true);
        actionToolbar.setTargetComponent(editor.getComponent());
        return actionToolbar;
    }


    /**
     * 创建deep coding 图标. 图标对应deep coding的不同模式入口
     * @param pattern pattern
     * @return AnAction
     */
    private AnAction createIcon(String pattern) {
        switch (pattern) {
            case Hot100Panel.HOT100:
                return (new com.xhf.leetcode.plugin.actions.AbstractAction(BundleUtils.i18n("leetcode.hot.100"), BundleUtils.i18n("leetcode.hot.100"), IconLoader.getIcon("/icons/m_hot100.png", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                    }

                });
            case Interview150Panel.INTER150:
                return (new com.xhf.leetcode.plugin.actions.AbstractAction(BundleUtils.i18n("classic.interview.150"), BundleUtils.i18n("classic.interview.150"), IconLoader.getIcon("/icons/m_mianshi150.png", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                    }

                });
            case LCCompetitionPanel.LC_COMPETITION:
                return (new com.xhf.leetcode.plugin.actions.AbstractAction(BundleUtils.i18n("leetcode.competition"), BundleUtils.i18n("leetcode.competition"), IconLoader.getIcon("/icons/m_LeetCode_Cup.png", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                    }

                });
            default:
                LogUtils.warn("什么鬼? pattern 传了个啥? 系统不认识! pattern = " + pattern);
                return new com.xhf.leetcode.plugin.actions.AbstractAction("Leetcode", "Leetcode", IconLoader.getIcon("/icons/pluginIcon.svg", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Leetcode Runner Setting");
                    }

                };
        }
    }

    /**
     * 创建下一题按钮
     * @return AnAction
     */
    private AnAction createNextAction() {
        return new com.xhf.leetcode.plugin.actions.AbstractAction(BundleUtils.i18n("deep.coding.next"), BundleUtils.i18n("deep.coding.next"), IconLoader.getIcon("/icons/right.svg", SplitTextEditorWithPreview.class)) {

            @Override
            public void doActionPerformed(Project project, AnActionEvent e) {
                try {
                    // 获取当前打开的文件
                    VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
                    // 为了避免Action内存泄露, 不使用外部变量
                    LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(cFile, project);
                    DeepCodingInfo dci = lc.getDeepCodingInfo();

                    // 打开下一道题目
                    assert project != null;
                    try {
                        doOpen(project, cFile, dci, true);
                    } catch (FileCreateError ex) {
                        LogUtils.error(ex);
                        ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("code.service.file.create.error"), true, true);
                    }
                } catch (Exception ex) {
                    LogUtils.error(ex);
                    Objects.requireNonNull(ConsoleUtils.getInstance(e.getProject())).showError("下一道题目打开错误! 错误原因 = " + ex.getMessage(), false, true);
                }
            }

        };
    }

    private MyList<Question> getData(DeepCodingInfo dci, Project project) {
        // 判断当前打开的是何种模式
        if (dci.getPattern().equals(Hot100Panel.HOT100)) {
            return  LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEEP_CODING_HOT_100_QUESTION_LIST);
        } else if (dci.getPattern().equals(Interview150Panel.INTER150)) {
            return  LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEEP_CODING_INTERVIEW_100_QUESTION_LIST);
        }
        return null;
    }

    /**
     * 打开文件. 同时定位到deep coding 模式的题目. 强制切换到deep coding对应模式的界面
     */
    private void doOpen(Project project, VirtualFile cFile, DeepCodingInfo dci, boolean isNext) throws FileCreateError {
        // 获取dci
        int len = dci.getTotalLength();
        int idx = dci.getIdx();
        int nextIdx;
        if (isNext) {
            nextIdx = (idx + 1) % len;
        } else {
            nextIdx = (idx - 1 + len) % len;
        }

        var dcqs = getDeepCodingQuestion(idx, nextIdx, dci, project);

        DeepCodingQuestion curQ = dcqs.curQ;
        DeepCodingQuestion nextQ = dcqs.nextQ;
        MyList<?> data = dcqs.data;
        // 判断是否匹配
        /*
         之所以需要判断是否匹配, 是因为dci在创建后, 实际显示的数据模型已经被更改
         eg:
         在创建dci时, 题目数据信息是完整的. 但当点击下一题按钮时, 题目数据信息已经被更改, 比如筛选. 此时
         在通过已有的dci信息打开下一题得不到正确结果, 因此需要终止. 并要求用户进行重定位
         重定位功能会清除所有的筛选条件, 得到最为原始的数据.

         如果dci匹配成功, 则表示dci创建时和点击下一题按钮时的数据模型是一致的, 无需重定位
         */
        if (! isMatch(curQ, CodeService.getInstance(project).parseTitleSlugFromVFile(cFile))) {
            ConsoleUtils.getInstance(project).showWaringWithoutConsole("当前题目无法与显示数据匹配, 请重定位当前文件", false, true);
            return;
        }
        // 显示下一道
        ViewUtils.scrollToVisibleOfMyList(data, nextIdx, true);
        // 下一道题目的dci
        DeepCodingInfo ndci = new DeepCodingInfo(dci.getPattern(), len, nextIdx);
        CodeService.getInstance(project).openCodeEditor(nextQ.toQuestion(project), ndci);
        // 打开对应的界面
        LCEventBus.getInstance().post(new DeepCodingTabChooseEvent(dci.getPattern()));
    }

    static class DeepCodingQuestions {
        DeepCodingQuestion curQ;
        DeepCodingQuestion nextQ;
        MyList<?> data;
    }

    private DeepCodingQuestions getDeepCodingQuestion(int idx, int nextIdx, DeepCodingInfo dci, Project project) {
        MyList<Question> data = getData(dci, project);
        DeepCodingQuestions dcqs = new DeepCodingQuestions();
        if (data != null) {
            ListModel<Question> model = data.getModel();
            if (idx < model.getSize()) {
                dcqs.curQ = model.getElementAt(idx);
            }
            if (nextIdx < model.getSize()) {
                dcqs.nextQ = model.getElementAt(nextIdx);
            }
            dcqs.data = data;
            return dcqs;
        } else  {
            // 获取LC-Competition Question
            var cq = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEEP_CODING_LC_COMPETITION_QUESTION_LIST);
            if (cq == null) {
                throw new RuntimeException("data is null!");
            }
            ListModel<CompetitionQuestion> model = cq.getModel();
            if (idx < model.getSize()) {
                dcqs.curQ = model.getElementAt(idx);
            }
            if (nextIdx < model.getSize()) {
                dcqs.nextQ = model.getElementAt(nextIdx);
            }
            dcqs.data = cq;
            return dcqs;
        }
    }

    /**
     * 判断当前idx指向题目的titleSlug是否能和传入的titleSlug匹配
     * @param titleSlug titleSlug
     * @return boolean
     */
    private boolean isMatch(DeepCodingQuestion curQ, String titleSlug) {
        // 判断titleSlug是否匹配
        if (curQ == null) {
            return false;
        }
        return curQ.getTitleSlug().equals(titleSlug);
    }

    /**
     * 创建上一题按钮
     * @return AnAction
     */
    private AnAction createPreAction() {
        return new AbstractAction(BundleUtils.i18n("deep.coding.pre"), BundleUtils.i18n("deep.coding.pre"), IconLoader.getIcon("/icons/left.svg", SplitTextEditorWithPreview.class)) {
            @Override
            public void doActionPerformed(Project project, AnActionEvent e) {
                try {
                    // 获取当前打开的文件
                    VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
                    // 为了避免Action内存泄露, 不使用外部变量
                    LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(cFile, project);
                    DeepCodingInfo dci = lc.getDeepCodingInfo();
                    // 打开下一道题目
                    assert project != null;
                    doOpen(project, cFile, dci, false);
                } catch (Exception ex) {
                    LogUtils.error(ex);
                    String msg = BundleUtils.i18nHelper("上一道题目打开错误! 错误原因 = " + ex.getMessage(), "Previous question open error! Reason: " + ex.getMessage());
                    Objects.requireNonNull(ConsoleUtils.getInstance(e.getProject())).showError(msg, false, true);
                } catch (FileCreateError ex) {
                    LogUtils.error(ex);
                    ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("code.service.file.create.error"), true, true);
                }
            }

        };
    }


    // TextEditor 接口特有
    @Override
    public @NotNull Editor getEditor() {
        return editor;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return component;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }

    @Override
    public @NotNull String getName() {
        return "Custom Text Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }

    @Override public void selectNotify() {}
    @Override public void deselectNotify() {}

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override public boolean isModified() { return false; }
    @Override public boolean isValid() { return true; }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {

    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    @Override
    public VirtualFile getFile() {
        return file;
    }

    /**
     * 别删除, 返回null. 否则无法适配低版本IntelliJ IDEA
     * @return null
     */
    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }
}