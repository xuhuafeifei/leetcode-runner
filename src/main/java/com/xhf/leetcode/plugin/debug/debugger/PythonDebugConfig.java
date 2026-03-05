package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;

/**
 * 现在设计层面做预留, 以后或许会有python相关特定的设置
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugConfig extends AbstractDebugConfig {

    private ReadType readType;
    private OutputType outputType;
    public PythonDebugConfig(InstReader reader, Output output) {
        super(reader, output);
    }

    public ReadType getReadType() {
        return readType;
    }

    public void setReadType(ReadType readType) {
        this.readType = readType;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public static class Builder extends AbstractDebugConfig.Builder<PythonDebugConfig> {

        public Builder(Project project) {
            super(project);
        }

        public PythonDebugConfig build() {
            PythonDebugConfig pythonDebugConfig = new PythonDebugConfig(super.reader, super.output);
            // 自动设置readType, outputType
            pythonDebugConfig.setReadType(ReadType.getByName(appSettings.getReadTypeName()));
            pythonDebugConfig.setOutputType(OutputType.getByName(super.appSettings.getOutputTypeName()));
            return pythonDebugConfig;
        }
    }
}
