package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.reader.InstReader;

/**
 * 现在设计层面做预留, 以后或许会有python相关特定的设置
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugConfig extends AbstractDebugConfig {
    public PythonDebugConfig(InstReader reader, Output output) {
        super(reader, output);
    }

    public static class Builder extends AbstractDebugConfig.Builder<PythonDebugConfig> {
        public Builder(Project project) {
            super(project);
        }

        public PythonDebugConfig build() {
            return new PythonDebugConfig(super.reader, super.output);
        }
    }
}
