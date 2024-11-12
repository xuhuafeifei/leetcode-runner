package com.xhf.leetcode.plugin.utils;

public enum LangType {
    JAVA("Java", ".java"),
    PYTHON("Python", ".py"),
    CPP("C++", ".cpp"),
    JAVASCRIPT("JavaScript", ".js"),
    C("C", ".c"),
    CPP14("C++14", ".cpp"),
    CPP17("C++17", ".cpp"),
    PYTHON3("Python3", ".py"),
    PYTHON2("Python2", ".py"),
    GO("Go", ".go"),
    RUST("Rust", ".rs")
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
