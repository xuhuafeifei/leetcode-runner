package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.xhf.leetcode.plugin.actions.AbstractAction;
import com.xhf.leetcode.plugin.bus.DeepCodingTabChooseEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.service.CodeService;
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
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SplitTextEditorWithPreview extends TextEditorWithPreview {

    public SplitTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview) {
        super(editor, preview, "Question " + Layout.SHOW_EDITOR_AND_PREVIEW.getName(), Layout.SHOW_EDITOR_AND_PREVIEW);

        // 注册断点监听器
        subscribeToBreakpointEvents(editor.getEditor());
    }

    private void subscribeToBreakpointEvents(Editor editor) {
        if (editor == null) {
            return;
        }
        Project project = editor.getProject();
        if (project == null) {
            return;
        }

        // 订阅断点事件
        // 自动与当前对象的生命周期绑定
        project.getMessageBus().connect(this)
                .subscribe(XBreakpointListener.TOPIC, new XBreakpointListener<XBreakpoint<?>>() {
                    @Override
                    public void breakpointAdded(@NotNull XBreakpoint<?> breakpoint) {
                        if (isBreakpointInEditor(breakpoint, editor)) {
                            XSourcePosition sp = breakpoint.getSourcePosition();
                            assert sp != null;
                            DebugUtils.simpleDebug("Breakpoint added in editor: " + sp.getFile().getPath() + " line "  + (sp.getLine() + 1), project, false);
                            InstSource.uiInstInput(DebugUtils.buildBInst(sp));
                        }
                    }

                    @Override
                    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
                        if (isBreakpointInEditor(breakpoint, editor)) {
                            XSourcePosition sp = breakpoint.getSourcePosition();
                            assert sp != null;
                            DebugUtils.simpleDebug("Breakpoint removed in editor: " + sp.getFile().getPath() + " line "  + (sp.getLine() + 1), project, false);
                            InstSource.uiInstInput(DebugUtils.buildRBInst(sp));
                        }
                    }

                    @Override
                    public void breakpointChanged(@NotNull XBreakpoint<?> breakpoint) {
                        if (isBreakpointInEditor(breakpoint, editor)) {
                            XSourcePosition sp = breakpoint.getSourcePosition();
                            assert sp != null;
                            DebugUtils.simpleDebug("Breakpoint added in editor: " + sp.getFile().getPath() + " line "  + (sp.getLine() + 1), project, false);
                        }
                    }
                });
    }

    private boolean isBreakpointInEditor(XBreakpoint<?> breakpoint, Editor editor) {
        if (breakpoint.getSourcePosition() == null) {
            return false;
        }

        // 检查断点是否属于当前 Editor
        String breakpointFilePath = breakpoint.getSourcePosition().getFile().getPath();
        // 获取 Editor 的文件路径
        // String editorFilePath = editor.getDocument().toString();
        String editorFilePath = ((EditorImpl) editor).getVirtualFile().getPath();
        return editorFilePath.contains(breakpointFilePath);
    }
    public String getFileContent() {
        return this.getEditor().getDocument().getText();
    }


    /**
     * 兼容 deep coding模式
     * 如果题目是通过deep coding模式打开, 那么会额外增加若干个按钮
     * @return action group
     */
    @Nullable
    protected ActionGroup createLeftToolbarActionGroup() {
        ActionGroup action = (ActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.editor.group");
        DefaultActionGroup dag = new DefaultActionGroup();
        dag.add(action);
        // 判断是否是通过deep coding模式创建
        try {
            VirtualFile file = super.getFile();
            Project project = super.getEditor().getProject();
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
        return dag;
    }

    /**
     * 创建deep coding 图标. 图标对应deep coding的不同模式入口
     * @param pattern pattern
     * @return AnAction
     */
    private AnAction createIcon(String pattern) {
        switch (pattern) {
            case Hot100Panel.HOT100:
                return (new AbstractAction("Leetcode 热题 100 道", "Leetcode 热题 100 道", IconLoader.getIcon("/icons/m_hot100.png", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                    }
                });
            case Interview150Panel.INTER150:
                return (new AbstractAction("经典面试 150 道", "经典面试 150 道", IconLoader.getIcon("/icons/m_mianshi150.png", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                    }
                });
            case LCCompetitionPanel.LC_COMPETITION:
                return (new AbstractAction("Leetcode 竞赛", "Leetcode 竞赛", IconLoader.getIcon("/icons/m_LeetCode_Cup.png", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                    }
                });
            default:
                LogUtils.warn("什么鬼? pattern 传了个啥? 系统不认识! pattern = " + pattern);
                return new AbstractAction("Leetcode", "Leetcode", IconLoader.getIcon("/icons/pluginIcon.svg", Hot100Panel.class)) {
                    @Override
                    public void doActionPerformed(Project project, AnActionEvent e) {
                    }
                };
        }
    }

    /**
     * 创建下一题按钮
     * @return AnAction
     */
    private AnAction createNextAction() {
        return new AbstractAction("Next", "Next question", IconLoader.getIcon("/icons/right.svg", SplitTextEditorWithPreview.class)) {
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
                    doOpen(project, cFile, dci, true);
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
    private void doOpen(Project project, VirtualFile cFile, DeepCodingInfo dci, boolean isNext) {
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
        return new AbstractAction("Pre", "Pre question", IconLoader.getIcon("/icons/left.svg", SplitTextEditorWithPreview.class)) {
            @Override
            public void doActionPerformed(Project project, AnActionEvent e) {
                try {
                    // 获取当前打开的文件
                    VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
                    // 为了避免Action内存泄露, 不使用外部变量
                    LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByVFile(cFile, project);
                    DeepCodingInfo dci = lc.getDeepCodingInfo();
                    // 获取dci
                    int len = dci.getTotalLength();
                    int idx = dci.getIdx();
                    int preIdx = (idx - 1 + len) % len;
                    // 打开下一道题目
                    assert project != null;
                    doOpen(project, cFile, dci, false);
                } catch (Exception ex) {
                    LogUtils.error(ex);
                    Objects.requireNonNull(ConsoleUtils.getInstance(e.getProject())).showError("上一道题目打开错误! 错误原因 = " + ex.getMessage(), false, true);
                }
            }
        };
    }
}
