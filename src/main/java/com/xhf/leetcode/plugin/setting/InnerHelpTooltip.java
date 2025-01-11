package com.xhf.leetcode.plugin.setting;

import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;

/**
   * 创建一个末尾携带help tooltip的组件
   */
  class InnerHelpTooltip {
    private final JPanel targetComponent;

    public InnerHelpTooltip() {
      targetComponent = new JPanel();
    }

    /**
     * @param flowType FlowLayout.LEFT, FlowLayout.RIGHT, FlowLayout.CENTER
     * @return InnerHelpTooltip
     */
    public static InnerHelpTooltip FlowLayout(int flowType) {
      InnerHelpTooltip innerHelpTooltip = new InnerHelpTooltip();
      innerHelpTooltip.targetComponent.setLayout(new FlowLayout(flowType));
      return innerHelpTooltip;
    }

    public static InnerHelpTooltip BorderLayout() {
      InnerHelpTooltip innerHelpTooltip = new InnerHelpTooltip();
      innerHelpTooltip.targetComponent.setLayout(new BorderLayout());
      return innerHelpTooltip;
    }

    public static InnerHelpTooltip BoxLayout() {
      InnerHelpTooltip innerHelpTooltip = new InnerHelpTooltip();
      innerHelpTooltip.targetComponent.setLayout(new BoxLayout(innerHelpTooltip.targetComponent, BoxLayout.X_AXIS));
      return innerHelpTooltip;
    }

    public InnerHelpTooltip add(JComponent component) {
      targetComponent.add(component);
      return this;
    }

    public InnerHelpTooltip addHelp(String text) {
      JBLabel helpIcon = new JBLabel(AllIcons.General.ContextHelp);
      HelpTooltip helpTooltip = new HelpTooltip();
      helpTooltip.setDescription(text).installOn(helpIcon);
      targetComponent.add(helpIcon);
      return this;
    }

    public JPanel getTargetComponent() {
      return targetComponent;
    }
  }