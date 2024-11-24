package com.xhf.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractMouseAdapter extends MouseAdapter {
    protected final Project project;
    public AbstractMouseAdapter(Project project) {
        this.project  = project;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            doubleClicked(e);
        }
    }

    protected abstract void doubleClicked(MouseEvent e);
}
