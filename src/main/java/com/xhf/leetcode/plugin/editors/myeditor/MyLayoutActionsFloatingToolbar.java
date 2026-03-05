package com.xhf.leetcode.plugin.editors.myeditor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.JBColor;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class MyLayoutActionsFloatingToolbar extends JPanel implements Disposable {

    private static final float BACKGROUND_ALPHA = 0.75f;
    private static final Color BACKGROUND = JBColor.namedColor("Toolbar.Floating.background",
        new JBColor(new Color(0xEDEDED), new Color(0x454A4D)));
    private final ToolbarVisibilityController visibilityController;

    public MyLayoutActionsFloatingToolbar(JComponent parentComponent, ActionGroup actionGroup) {
        setLayout(new BorderLayout());
        setOpaque(false);

        ActionToolbar actionToolbar = ActionManager.getInstance()
            .createActionToolbar("myLayoutActionsFloatingToolbar", actionGroup, true);
        actionToolbar.setTargetComponent(parentComponent);
        actionToolbar.setReservePlaceAutoPopupIcon(false);
        actionToolbar.setMinimumButtonSize(new Dimension(22, 22));
        // actionToolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
        // actionToolbar.setSkipWindowAdjustments(true);

        add(actionToolbar.getComponent(), BorderLayout.CENTER);

        this.visibilityController = new ToolbarVisibilityController(false, parentComponent, this);

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
    public void dispose() {
    }
}
