package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractDebugConfig implements DebugConfig {
    private InstReader reader;
    private Output output;

    public AbstractDebugConfig(InstReader reader, Output output) {
        this.reader = reader;
        this.output = output;
    }

    @Override
    public InstReader getReader() {
        return reader;
    }

    @Override
    public void setReader(InstReader reader) {
        this.reader = reader;
    }

    @Override
    public Output getOutput() {
        return output;
    }

    @Override
    public void setOutput(Output output) {
        this.output = output;
    }

    public static abstract class Builder<T extends AbstractDebugConfig> {
        protected final Project project;
        protected InstReader reader;
        protected Output output;
        protected AppSettings appSettings;

        public Builder(Project project) {
            this.project = project;
            this.appSettings = AppSettings.getInstance();
        }

        public Builder<T> setReader(InstReader reader) {
            this.reader = reader;
            return this;
        }
        public Builder<T> setOutput(Output output) {
            this.output = output;
            return this;
        }

        public Builder<T> autoBuild() {

            return
                    setReader(
                            ReadType.getReaderInstanceByTypeName(appSettings.getReadTypeName(), project)
                    ).setOutput(
                            OutputType.getOutputInstanceByTypeName(appSettings.getOutputTypeName(), project)
                    );
        }

        public abstract T build();
    }
}
