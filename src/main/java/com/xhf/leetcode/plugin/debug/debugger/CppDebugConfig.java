package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.reader.InstReader;

/**
 * 没想好要做哪些拓展, 先这么预留着
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppDebugConfig extends AbstractDebugConfig {

    public CppDebugConfig(InstReader reader, Output output) {
        super(reader, output);
    }

    public static class Builder extends AbstractDebugConfig.Builder<CppDebugConfig> {

        public Builder(Project project) {
            super(project);
        }

        @Override
        public CppDebugConfig build() {
            return new CppDebugConfig(super.reader, super.output);
        }
    }
}
