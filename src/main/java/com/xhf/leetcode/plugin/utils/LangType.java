package com.xhf.leetcode.plugin.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public enum LangType {
    JAVA("java", ".java"),
    PYTHON("python", ".py"),
    CPP("c++", ".cpp"),
    JAVASCRIPT("javascript", ".js"),
    C("c", ".c"),
    PYTHON3("python3", ".py"),
    GO("go", ".go"),
    ;

    private LangType(String langType, String suffix) {
        this.langType = langType;
        this.suffix = suffix;
    }

    private String langType;
    private String suffix;

    public static String getAllLangType() {
        LangType[] values = LangType.values();
        StringBuilder sb = new StringBuilder();
        for (LangType langType : values) {
            sb.append(langType.getLangType()).append(",");
        }
        return sb.toString();
    }

    /**
     * 匹配给定langType是否被允许.
     * @param langType
     * @return
     */
    public static boolean contains(String langType) {
        if (StringUtils.isBlank(langType)) {
            return false;
        }
        for (LangType lt : LangType.values()) {
            if (lt.langType.equalsIgnoreCase(langType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过文件后缀返回对应的langType
     * @param suffix
     * @return
     */
    public static String convertBySuffix(String suffix) {
        for (LangType lt : LangType.values()) {
            if (lt.suffix.equals(suffix)) {
                return lt.langType;
            }
        }
        return null;
    }

    public static boolean equals(String langType, String settingLangType) {
        boolean flag = langType.equalsIgnoreCase(settingLangType);
        // python特判
        if (! flag && langType.contains("python") && settingLangType.contains("python")) {
            return true;
        }
        return false;
    }

    public String getLangType() {
        return langType;
    }

    public String getSuffix() {
        return suffix;
    }
}
