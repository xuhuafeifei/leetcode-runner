package com.xhf.leetcode.plugin.setting;

import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.ui.components.JBLabel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 创建一个末尾携带help tooltip的组件
 * 同时支持不同的布局方式
 * <p>
 * 基本使用方式:
 * JPanel targetComponent = InnerHelpTooltip.BoxLayout()
 * .add(myFileBrowserBtn)
 * .addHelp(HELP_CONTENT)
 * .getTargetComponent();
 * 先调用BoxLayout()设置布局
 * 然后调用add()方法, 将组件添加到目标组件当中
 * 然后调用addHelp()方法, 将需要的help内容写入
 * 最后调用getTargetComponent()获取被help tooltip修饰的目标组件
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class InnerHelpTooltip {

    private final JPanel targetComponent;
    private boolean flag = false;

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

    public static InnerHelpTooltip FlowLayout(int flowType, int hgap, int vgap) {
        InnerHelpTooltip innerHelpTooltip = new InnerHelpTooltip();
        innerHelpTooltip.targetComponent.setLayout(new FlowLayout(flowType, hgap, vgap));
        return innerHelpTooltip;
    }

    public static InnerHelpTooltip DefaultLayout() {
        return new InnerHelpTooltip();
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
        if (flag) {
            throw new RuntimeException("InnerHelpTooltip使用错误! 只允许存在一个Tooltip");
        }
        flag = true;
        JBLabel helpIcon = new JBLabel(AllIcons.General.ContextHelp);
        HelpTooltip helpTooltip = new HelpTooltip();
        helpTooltip.setNeverHideOnTimeout(true);
        helpTooltip.setDescription(text).installOn(helpIcon);
        targetComponent.add(helpIcon);
        return this;
    }

    public InnerHelpTooltip addHelpWithLink(String text, String linkText, URL linkURL) {
        if (flag) {
            throw new RuntimeException("InnerHelpTooltip使用错误! 只允许存在一个Tooltip");
        }
        flag = true;
        JBLabel helpIcon = new JBLabel(AllIcons.General.ContextHelp);
        HelpTooltip helpTooltip = new HelpTooltip();
        helpTooltip.setLink(linkText, () -> {
            System.out.println("abab");
        });
        helpTooltip.setBrowserLink(linkText, linkURL);
        helpTooltip.setNeverHideOnTimeout(true);
        helpTooltip.setDescription(text).installOn(helpIcon);
        targetComponent.add(helpIcon);
        return this;
    }

    public JPanel getTargetComponent() {
        return targetComponent;
    }
}