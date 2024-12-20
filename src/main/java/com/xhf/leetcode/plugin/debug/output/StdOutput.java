package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdOutput extends IOOutput{
    public StdOutput(Project project) {
        super(project);
    }

    @Override
    protected void outputTo(String output) {
        System.out.println(output);
    }
}
