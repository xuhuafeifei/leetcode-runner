package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LangType;

import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractVariableConvertor implements VariableConvertor{
    protected final AppSettings appSettings = AppSettings.getInstance();

    @Override
    public final String convert(String testcase, String variableName) {
        LangType langType = LangType.getType(appSettings.getLangType());
        switch(Objects.requireNonNull(langType)) {
            case JAVA:
                return doJava(testcase, variableName);
            case PYTHON3:
                return doPython(testcase, variableName);
            default:
                throw new DebugError("目前不支持 " + langType.getLangType() + " 语言的debug");
        }
    }

    protected abstract String doPython(String testcase, String variableName);

    protected abstract String doJava(String testcase, String variableName);
}
