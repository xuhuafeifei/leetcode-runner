package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.options.ShowSettingsUtil;
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
import com.xhf.leetcode.plugin.editors.myeditor.MyTextEditorWithPreview;
import com.xhf.leetcode.plugin.editors.text.CustomTextEditor;
import com.xhf.leetcode.plugin.editors.text.CustomTextEditorProvider;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.*;
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
public class SplitTextEditorWithPreview extends MyTextEditorWithPreview {

    private final Project project;

    public SplitTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview, Project project) {
        super(editor, preview, "Question " + Layout.SHOW_EDITOR_AND_PREVIEW.getName(), Layout.SHOW_EDITOR_AND_PREVIEW);
        this.project = project;
        // 注册断点监听器
        subscribeToBreakpointEvents(editor.getEditor());
    }

    @Nullable
    protected ActionGroup createRightToolbarActionGroup() {
        ActionGroup action = (ActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.editor.basic.group");
        return action;
    }

    @NotNull
    protected ActionGroup createViewActionGroup() {
        return new DefaultActionGroup(
                getShowEditorAction(),
                getShowEditorAndPreviewAction(),
                getShowPreviewAction()
        );
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
                            String msg = BundleUtils.i18nHelper("断点以添加到文件 " + sp.getFile().getPath() + " 第" + (sp.getLine() + 1) + "行处", "Breakpoint added in editor: " + sp.getFile().getPath() + " line " + (sp.getLine() + 1));
                            DebugUtils.simpleDebug(msg, project, false);
                            InstSource.uiInstInput(DebugUtils.buildBInst(sp));
                        }
                    }

                    @Override
                    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
                        if (isBreakpointInEditor(breakpoint, editor)) {
                            XSourcePosition sp = breakpoint.getSourcePosition();
                            assert sp != null;
                            String msg = BundleUtils.i18nHelper("断点以添加到文件 " + sp.getFile().getPath() + " 第" + (sp.getLine() + 1) + "行处", "Breakpoint added in editor: " + sp.getFile().getPath() + " line " + (sp.getLine() + 1));
                            DebugUtils.simpleDebug(msg, project, false);
                            InstSource.uiInstInput(DebugUtils.buildRBInst(sp));
                        }
                    }

                    @Override
                    public void breakpointChanged(@NotNull XBreakpoint<?> breakpoint) {
                        if (isBreakpointInEditor(breakpoint, editor)) {
                            XSourcePosition sp = breakpoint.getSourcePosition();
                            assert sp != null;
                            String msg = BundleUtils.i18nHelper("断点以添加到文件 " + sp.getFile().getPath() + " 第" + (sp.getLine() + 1) + "行处", "Breakpoint added in editor: " + sp.getFile().getPath() + " line " + (sp.getLine() + 1));
                            DebugUtils.simpleDebug(msg, project, false);
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
        // String editorFilePath = ((EditorImpl) editor).getVirtualFile().getPath();
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
        return null;
    }
}
