package com.xhf.leetcode.plugin.editors.myeditor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LayoutActionsFloatingToolbar extends ActionToolbarImpl implements Disposable {
    private final ToolbarVisibilityController visibilityController;
    private static final float BACKGROUND_ALPHA = 0.75f;
    private static final Color BACKGROUND = JBColor.namedColor("Toolbar.Floating.background",
            new JBColor(new Color(0xEDEDED), new Color(0x454A4D)));

    public LayoutActionsFloatingToolbar(JComponent parentComponent, ActionGroup actionGroup) {
        super(ActionPlaces.CONTEXT_TOOLBAR, actionGroup, true);

        this.visibilityController = new ToolbarVisibilityController(false, parentComponent, this);
//        Disposer.register(this, visibilityController);

        setTargetComponent(parentComponent);
        setReservePlaceAutoPopupIcon(false);
        setMinimumButtonSize(new Dimension(22, 22));
        setSkipWindowAdjustments(true);
        setOpaque(false);
        setLayoutPolicy(NOWRAP_LAYOUT_POLICY);

        // 同步位置
        parentComponent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds(parentComponent);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updateBounds(parentComponent);
            }
        });
        updateBounds(parentComponent);
    }

    private void updateBounds(JComponent parent) {
        Dimension size = getPreferredSize();
        int x = parent.getWidth() - size.width - 16;
        int y = 12;
        setBounds(x, y, size.width, size.height);
    }

    public ToolbarVisibilityController getVisibilityController() {
        return visibilityController;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            float alpha = visibilityController.getOpacity() * BACKGROUND_ALPHA;
            if (alpha == 0.0f) {
                updateActionsImmediately();
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BACKGROUND);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            float alpha = visibilityController.getOpacity() * BACKGROUND_ALPHA;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintChildren(g2);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateActionsImmediately(true);
    }

    @Override
    public void dispose() {
        // 没有额外资源需要释放
    }
}
