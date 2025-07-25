package com.xhf.leetcode.plugin.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public enum LangType {
    JAVA("java", ".java", "//", new String[]{"java", "Java"}),
    //    PYTHON("python", ".py", "#"),
    CPP("cpp", ".cpp", "//", new String[]{"cpp", "c++", "C++", "Cpp", "CPP"}),
    JAVASCRIPT("javascript", ".js", "//", new String[]{"js"}),
    C("c", ".c", "//", new String[]{"c", "C"}),
    PYTHON3("python3", ".py", "#", new String[]{"py", "Python3", "python3"}), // rec内容别加'python', 否则后续代码解析会报错
    GO("go", ".go", "//", new String[]{"go", "Go", "go", "GO"}),
    ;


    LangType(String langType, String suffix, String commentSymbol, String[] rec) {
        this.langType = langType;
        this.suffix = suffix;
        this.commentSymbol = commentSymbol;
        this.rec = rec;
    }

    private final String langType;
    private final String suffix;
    private final String commentSymbol;
    private final String[] rec;

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
     */
    public static String convertBySuffix(String suffix) {
        for (LangType lt : LangType.values()) {
            if (lt.suffix.equals(suffix)) {
                return lt.langType;
            }
        }
        return null;
    }

    public static boolean equals(@Nullable String langType, String settingLangType) {
        if (langType == null) {
            return false;
        }
        boolean flag = langType.equalsIgnoreCase(settingLangType);
        if (flag) {
            return true;
        }
        // python特判
        return langType.contains("python") && settingLangType.contains("python");
    }

    public static LangType getType(String langType) {
        // 遍历所有的langType进行匹配, 匹配成功返回LangType
        for (LangType lt : LangType.values()) {
            if (lt.langType.equalsIgnoreCase(langType)) {
                return lt;
            }
        }
        return null;
    }

    public String getLangType() {
        return langType;
    }

    public String getSuffix() {
        return suffix;
    }

    public static String getCommentSymbol(String langType) {
        for (LangType lt : LangType.values()) {
            if (lt.langType.equals(langType)) {
                return lt.commentSymbol;
            }
        }
        return null;
    }

    /**
     * langType是否是当前LangType
     *
     * @param lang langType
     * @return boolean
     */
    public boolean has(String lang) {
        for (String _rec : this.rec) {
            if (_rec.equals(lang)) {
                return true;
            }
        }
        return false;
    }
}
