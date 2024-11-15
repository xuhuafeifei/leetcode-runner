package com.xhf.leetcode.plugin.utils;

public enum LangType {
    JAVA("java", ".java"),
    PYTHON("python", ".py"),
    CPP("cpp", ".cpp"),
    JAVASCRIPT("javascript", ".js"),
    C("c", ".c"),
    PYTHON3("python3", ".py"),
    GO("golang", ".go"),
    ;

    private LangType(String langType, String suffix) {
        this.langType = langType;
        this.suffix = suffix;
    }

    private String langType;
    private String suffix;

    public String getLangType() {
        return langType;
    }

    public String getSuffix() {
        return suffix;
    }
}
