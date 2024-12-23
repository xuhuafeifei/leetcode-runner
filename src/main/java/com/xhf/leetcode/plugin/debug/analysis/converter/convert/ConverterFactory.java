package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

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
        for (ParamType value : ParamType.values()) {
            string2ConvertorMap.put(value.getType(), value.getConvertor());
        }
    }

    /**
     * 通过分析器得到的参数类型, 获取对应的converter
     * @param type type是分析器分析得到的参数类型, 不代表项目对于某一类型的标识
     * @return convertor
     */
    public VariableConvertor createVariableConvertor(String type) {
        // 通过ParamType获取类型的唯一标识
        ParamType byType = ParamType.getByType(type);
        if (byType == null) {
            return null;
        }
        return string2ConvertorMap.getOrDefault(byType.getType(), null);
    }
}
