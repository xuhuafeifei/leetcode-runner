package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.ReadType;

/**
 * 没想好要做哪些拓展, 先这么预留着
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppDebugConfig extends AbstractDebugConfig {

    public CppDebugConfig(InstReader reader, Output output) {
        super(reader, output);
    }

    private ReadType readType;
    private OutputType outputType;

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

    public static class Builder extends AbstractDebugConfig.Builder<CppDebugConfig> {
        public Builder(Project project) {
            super(project);
        }

        public CppDebugConfig build() {
            var cppDebugConfig = new CppDebugConfig(super.reader, super.output);
            // 自动设置readType, outputType
            cppDebugConfig.setReadType(ReadType.getByName(appSettings.getReadTypeName()));
            cppDebugConfig.setOutputType(OutputType.getByName(super.appSettings.getOutputTypeName()));
            return cppDebugConfig;
        }
    }
}
