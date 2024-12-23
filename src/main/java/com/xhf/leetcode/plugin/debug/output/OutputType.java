package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.reader.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * 命令读取来源
 */
public enum OutputType {
    /**
     * 标准输入读取
     */
    STD_OUT("std_out", StdOutput.class, "标准输出显示"),
    /**
     * 命令行读取
     */
    CONSOLE_OUT("console_out", ConsoleOutput.class, "console显示"),
    /**
     * ui输入
     */
    UI_OUT("ui_out", UIOutput.class, "UI显示")
    ;
    private final Class<? extends Output> output;
    private String type;
    private String name;

    OutputType(String type, Class<? extends Output> output, String name) {
        this.type = type;
        this.output = output;
        this.name = name;
    }

    public static String[] getNames() {
        String[] names = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            names[i] = values()[i].getName();
        }
        return names;
    }

    public static OutputType getByName(String name) {
        for (OutputType outputType : values()) {
            if (outputType.getName().equals(name)) {
                return outputType;
            }
        }
        return null;
    }

    /**
     * 通过output子类构造函数创建
     * @param outputTypeName
     * @param project
     * @return
     */
    public static Output getOutputInstanceByTypeName(@NotNull String outputTypeName, @NotNull Project project) {
        OutputType outputType = getByName(outputTypeName);
        try {
            assert outputType != null;
            return outputType.output.getDeclaredConstructor(Project.class).newInstance(project);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getType() {
        return type;
    }

    public Class<? extends Output> getOutput() {
        return output;
    }

    public String getName() {
        return this.name;
    }
}
