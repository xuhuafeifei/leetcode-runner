package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;

/**
 * 这个是项目运行时的标准输入输出
 *
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
