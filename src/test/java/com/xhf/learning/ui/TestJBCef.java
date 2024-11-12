package com.xhf.learning.ui;

import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.jcef.JBCefBrowser;
import com.xhf.learning.okhttp.URLUtils;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

public class TestJBCef {
    @Test
    public void test1() throws InterruptedException {
        Window activeFrame = IdeFrameImpl.getActiveFrame();
        if (activeFrame == null) {
            return;
        }
        Rectangle bounds = activeFrame.getGraphicsConfiguration().getBounds();
        final JFrame frame = new IdeFrameImpl();
        frame.setTitle("JCEF login");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(2);
        frame.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
        frame.setLayout(new BorderLayout());

        loadJCEFComponent(frame);

        frame.setVisible(true);
    }

    private void loadJCEFComponent(JFrame frame) {
        final JBCefBrowser jbCefBrowser = new JBCefBrowser(URLUtils.getLeetcodeLogin());

        frame.add(jbCefBrowser.getComponent(), BorderLayout.CENTER);
    }
}
