package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.params.Operation;

/**
 * 所有的reader必须继承AbstractInstReader
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractInstReader implements InstReader{
    protected final Project project;
    protected final ReadType readType;

    public AbstractInstReader(Project project, ReadType readType) {
        this.project = project;
        this.readType = readType;
    }
}
