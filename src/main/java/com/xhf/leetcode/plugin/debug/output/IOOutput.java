package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class IOOutput extends AbstractOutput{
    public IOOutput(Project project) {
        super(project);
    }

    @Override
    public final void output(ExecuteResult r) {
        if (r.isSuccess()) {
            if (r.isHasResult()) {
                handleOutput(r.getResult());
            }
        } else {
            handleOutput(r.getMsg());
        }
    }

    private void handleOutput(String output) {
        // 检测末尾是否是换行
        if (!output.endsWith("\n")) {
            output += "\n";
        }
        outputTo(output);
    }

    /**
     * 允许子类确定不同的输出位置
     * @param output 输出内容
     */
    protected abstract void outputTo(String output);
}
