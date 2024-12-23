package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;

/**
 * 所有output必须继承他, 并且子类必须有一个只有参数：Project的构造函数. 原因可以参考{@link OutputType#getOutputInstanceByTypeName}
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
