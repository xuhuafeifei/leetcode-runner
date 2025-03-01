package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LangType;

import java.util.Objects;

/**
 * 抽象变量转换器, 允许将测试案例转换为对应语言的代码
 *
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
            case CPP:
                return doCpp(testcase, variableName);
            default:
                throw new DebugError(langType.getLangType() + " " + BundleUtils.i18n("debug.leetcode.notsupport"));
        }
    }

    protected abstract String doCpp(String testcase, String variableName);

    protected abstract String doPython(String testcase, String variableName);

    protected abstract String doJava(String testcase, String variableName);


    protected String addTab(String content) {
        StringBuilder sb = new StringBuilder();
        for (String s : content.split("\n")) {
            sb.append(TAB).append(s).append("\n");
        }
        return sb.toString();
    }
}
