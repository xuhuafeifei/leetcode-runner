package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.setting.LanguageConvertor;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.lang.reflect.InvocationTargetException;

/**
 * 命令读取来源
 */
public enum ReadType {
    /**
     * 标准输入读取
     */
    STD_IN("std_in", StdInReader.class,
        BundleUtils.i18nHelper(LanguageConvertor.STD_INPUT_ZH, LanguageConvertor.STD_INPUT_EN)),
    /**
     * 命令行读取
     */
    COMMAND_IN("command_in", CommandLineReader.class,
        BundleUtils.i18nHelper(LanguageConvertor.COMMAND_LINE_ZH, LanguageConvertor.COMMAND_LINE_EN)),
    /**
     * UI输入
     */
    UI_IN("ui_in", UIReader.class, BundleUtils.i18nHelper(LanguageConvertor.UI_READ_ZH, LanguageConvertor.UI_READ_EN));
    private final Class<? extends InstReader> reader;
    private final String type;
    private final String name;

    ReadType(String type, Class<? extends InstReader> reader, String name) {
        this.type = type;
        this.reader = reader;
        this.name = name;
    }

    public static String[] getNames() {
        String[] names = new String[ReadType.values().length];
        for (int i = 0; i < ReadType.values().length; i++) {
            names[i] = ReadType.values()[i].getName();
        }
        return names;
    }

    public static ReadType getByName(String name) {
        for (ReadType readType : ReadType.values()) {
            if (readType.getName().equals(name)) {
                return readType;
            }
        }
        return null;
    }

    public static InstReader getReaderInstanceByTypeName(String name, Project project) {
        ReadType readType = getByName(name);
        assert readType != null;
        try {
            return readType.reader.getDeclaredConstructor(Project.class).newInstance(project);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getType() {
        return type;
    }

    public Class<? extends InstReader> getReader() {
        return reader;
    }

    public String getName() {
        return this.name;
    }
}
