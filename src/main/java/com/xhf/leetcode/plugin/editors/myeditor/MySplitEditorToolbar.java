package com.xhf.leetcode.plugin.editors.myeditor;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.impl.EditorHeaderComponent;
import com.intellij.util.ui.JBInsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MySplitEditorToolbar extends EditorHeaderComponent {

  private final ActionToolbar myRightToolbar;
  private final boolean myLeftToolbarEmpty;

  public MySplitEditorToolbar(@Nullable ActionToolbar leftToolbar, @NotNull ActionToolbar rightToolbar) {
    super();
    setLayout(new GridBagLayout());
    myRightToolbar = rightToolbar;
    myLeftToolbarEmpty = leftToolbar == null || leftToolbar.getActions().isEmpty();

    if (leftToolbar != null) {
      add(leftToolbar.getComponent());
    }

    final JPanel centerPanel = new JPanel(new BorderLayout());
    add(centerPanel, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0,
                                            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new JBInsets(0, 0, 0, 0), 0, 0));

    add(myRightToolbar.getComponent());
  }

  public ActionToolbar getRightToolbar() {
    return myRightToolbar;
  }

  public boolean isLeftToolbarEmpty() {
    return myLeftToolbarEmpty;
  }

  public void refresh() {
    myRightToolbar.updateActionsImmediately();
  }
}
