package com.xhf.leetcode.plugin.editors;

import com.intellij.icons.AllIcons;
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
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.DeepCodingInfo;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import com.xhf.leetcode.plugin.window.deepcoding.Hot100Panel;
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
                if (lc.getDeepCodingInfo().getPattern().equals(Hot100Panel.HOT100)) {
                    dag.add(new AbstractAction("Leetcode 热题 100 道", "Leetcode 热题 100 道", IconLoader.getIcon("/icons/m_hot100.png", Hot100Panel.class)) {

                        @Override
                        public void doActionPerformed(Project project, AnActionEvent e) {

                        }
                    });
                }
                dag.add(createPreAction());
                dag.add(createNextAction());
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return dag;
    }

    private AnAction createNextAction() {
        return new AbstractAction("Next", "Next question", AllIcons.Chooser.Right) {
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
                    int nextIdx = (idx + 1) % len;
                    // 打开下一道题目
                    assert project != null;
                    MyList<Question> data = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEEP_CODING_HOT_100_QUESTION_LIST);
                    assert data != null;
                    ListModel<Question> model = data.getModel();
                    Question question = model.getElementAt(nextIdx);
                    // 下一道题目的dci
                    DeepCodingInfo ndci = new DeepCodingInfo(dci.getPattern(), len, nextIdx);
                    CodeService.getInstance(project).openCodeEditor(question, ndci);
                } catch (Exception ex) {
                    LogUtils.error(ex);
                    Objects.requireNonNull(ConsoleUtils.getInstance(e.getProject())).showError("下一道题目打开错误! 错误原因 = " + ex.getMessage(), false, true);
                }
            }
        };
    }

    private AnAction createPreAction() {
        return new AbstractAction("Pre", "Pre question", AllIcons.Chooser.Left) {
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
                    MyList<Question> data = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEEP_CODING_HOT_100_QUESTION_LIST);
                    assert data != null;
                    ListModel<Question> model = data.getModel();
                    Question question = model.getElementAt(preIdx);
                    // 下一道题目的dci
                    DeepCodingInfo ndci = new DeepCodingInfo(dci.getPattern(), len, preIdx);
                    CodeService.getInstance(project).openCodeEditor(question, ndci);
                } catch (Exception ex) {
                    LogUtils.error(ex);
                    Objects.requireNonNull(ConsoleUtils.getInstance(e.getProject())).showError("上一道题目打开错误! 错误原因 = " + ex.getMessage(), false, true);
                }
            }
        };
    }
}
