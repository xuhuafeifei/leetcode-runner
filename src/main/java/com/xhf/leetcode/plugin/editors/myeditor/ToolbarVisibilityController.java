package com.xhf.leetcode.plugin.editors.myeditor;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ToolbarVisibilityController extends VisibilityController {

    private final boolean autoHide;
    private final JComponent parentComponent;
    private final JComponent toolbarComponent;

    public ToolbarVisibilityController(boolean autoHide, JComponent parentComponent, JComponent toolbarComponent) {
        this.autoHide = autoHide;
        this.parentComponent = parentComponent;
        this.toolbarComponent = toolbarComponent;
    }

    @Override
    protected boolean isAutoHide() {
        return autoHide;
    }

    @Override
    protected boolean isRetention() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return false;
        }

        Point location = pointerInfo.getLocation();
        SwingUtilities.convertPointFromScreen(location, parentComponent);
        Rectangle bounds = new Rectangle(0, 0, parentComponent.getWidth(), parentComponent.getHeight());
        return bounds.contains(location);
    }

    @Override
    protected void repaintComponent() {
        toolbarComponent.repaint();
    }

    @Override
    protected void setVisibleFlag(boolean visible) {
        toolbarComponent.setVisible(visible);
    }
}
