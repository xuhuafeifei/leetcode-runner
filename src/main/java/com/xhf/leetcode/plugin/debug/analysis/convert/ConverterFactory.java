package com.xhf.leetcode.plugin.debug.analysis.convert;

import java.util.HashMap;
import java.util.Map;

/**
 * converter工厂, 根据不同的参数类型返回对应的converter
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ConverterFactory {
    private static final Map<String, VariableConvertor> string2ConvertorMap = new HashMap<>();

    // 单例模式
    private static final ConverterFactory instance = new ConverterFactory();
    private ConverterFactory() {}
    public static ConverterFactory getInstance() {
        return instance;
    }

    static {
        string2ConvertorMap.put("int", new IntConvertor());
        string2ConvertorMap.put("int[]", new IntArrayConvertor());
        string2ConvertorMap.put("int[][]", new IntMatrixConvertor());
        string2ConvertorMap.put("String", new StringConvertor());
        string2ConvertorMap.put("String[]", new StringArrayConvertor());
        string2ConvertorMap.put("String[][]", new StringMatrixConvertor());
    }

    public VariableConvertor createVariableConvertor(String paramType) {
        return string2ConvertorMap.get(paramType);
    }
}
