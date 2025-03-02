package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * 命令读取来源
 */
public enum OutputType {
    /**
     * 标准输入读取
     */
    STD_OUT("std_out", StdOutput.class, BundleUtils.i18nHelper("标准输出显示", "Std display")),
    /**
     * 命令行读取
     */
    CONSOLE_OUT("console_out", ConsoleOutput.class, BundleUtils.i18nHelper("console显示", "Console display")),
    /**
     * ui输入
     */
    UI_OUT("ui_out", UIOutput.class, BundleUtils.i18nHelper("UI显示", "UI display"))
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
     * @param outputTypeName outputType
     * @param project project
     * @return output
     */
    public static Output getOutputInstanceByTypeName(@NotNull String outputTypeName, @NotNull Project project) {
        OutputType outputType = getByName(outputTypeName);
        try {
            assert outputType != null;
            // 通过output的构造函数创建output. 所有output子类必须继承AbstractOutput, 并且必须有Project的构造函数
            return outputType.output.getDeclaredConstructor(Project.class).newInstance(project);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertToCN(String outputTypeName) {
        if (outputTypeName == null) {
            return null;
        }
        if (outputTypeName.equals("Std Display")) {
            return "标准输出显示";
        }
        if (outputTypeName.equals("Console display")) {
            return "console显示";
        }
        if (outputTypeName.equals("UI display")) {
            return "UI显示";
        }
        return outputTypeName;
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
