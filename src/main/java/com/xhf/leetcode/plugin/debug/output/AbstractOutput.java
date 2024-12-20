package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;

/**
 * 所有output必须继承他, 并且只能存在一个构造函数
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractOutput implements Output{
    protected final Project project;
    public AbstractOutput(Project project) {
        this.project = project;
    }
    public abstract void output(ExecuteResult r);
}
