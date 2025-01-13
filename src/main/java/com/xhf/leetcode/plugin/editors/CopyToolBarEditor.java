package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.SplitEditorToolbar;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 拥有包含copy能力的toolbar的fileEditor
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class CopyToolBarEditor implements FileEditor {

    /**
     * support copy code ability
     */
    protected final SplitEditorToolbar createToolbarWrapper(JComponent comp) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(copyAction(), copyToAction());
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Solution", actionGroup, true);
        actionToolbar.setTargetComponent(comp);
        ActionToolbar empty = ActionManager.getInstance().createActionToolbar("Empty", new DefaultActionGroup(), true);
        empty.setTargetComponent(comp);
        return new SplitEditorToolbar(actionToolbar, empty);
    }

    /**
     * 粘贴到别处的能力
     * @return
     */
    protected abstract AnAction copyToAction();

    /**
     * 粘贴内容到剪切板的能力
     * @return
     */
    protected abstract AnAction copyAction();


    @Override
    public void setState(@NotNull FileEditorState state) {

    }
}
