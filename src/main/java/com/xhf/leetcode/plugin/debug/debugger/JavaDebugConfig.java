package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.ConsoleOutput;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.CommandLineReader;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugConfig {
    private InstReader reader;
    private Output output;

    public JavaDebugConfig(InstReader reader, Output output) {
        this.reader = reader;
        this.output = output;
    }

    public InstReader getReader() {
        return reader;
    }

    public void setReader(InstReader reader) {
        this.reader = reader;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public static class Builder {
        private final Project project;
        private InstReader reader;
        private Output output;

        public Builder(Project project) {
            this.project = project;
        }

        public Builder setReader(InstReader reader) {
            this.reader = reader;
            return this;
        }
        public Builder setOutput(Output output) {
            this.output = output;
            return this;
        }
        // todo: 自动根据项目需求构建. 目前先写死
        public Builder autoBuild() {
            AppSettings appSettings = AppSettings.getInstance();

            return
                    setReader(
                            ReadType.getReaderInstanceByTypeName(appSettings.getReadTypeName(), project)
                    ).setOutput(
                            OutputType.getOutputInstanceByTypeName(appSettings.getOutputTypeName(), project)
                    );
        }

        public JavaDebugConfig build() {
            return new JavaDebugConfig(reader, output);
        }
    }
}
