package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        String editorFilePath = editor.getDocument().toString();
        return editorFilePath.contains(breakpointFilePath);
    }
    public String getFileContent() {
        return this.getEditor().getDocument().getText();
    }


    @Nullable
    protected ActionGroup createLeftToolbarActionGroup() {
        return (ActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.editor.group");
    }
}
