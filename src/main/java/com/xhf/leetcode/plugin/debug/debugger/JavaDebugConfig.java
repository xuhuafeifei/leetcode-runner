package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * 没想好要做哪些拓展, 先这么预留着
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugConfig extends AbstractDebugConfig {

    public JavaDebugConfig(InstReader reader, Output output) {
        super(reader, output);
    }

    public static class Builder extends AbstractDebugConfig.Builder<JavaDebugConfig> {

        public Builder(Project project) {
            super(project);
        }

        @Override
        public JavaDebugConfig build() {
            return new JavaDebugConfig(super.reader, super.output);
        }
    }
}
