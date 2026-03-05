package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import java.util.HashMap;
import java.util.Map;

/**
 * converter工厂, 根据不同的参数类型返回对应的converter
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 * 《Effective Java》推荐枚举方式实现单例，不仅能避免多线程同步问题，而且还能防止反序列化重新创建新的对象
 */
public enum ConverterFactory {

    INSTANCE; // 枚举单例实例
    private final Map<String, VariableConvertor> string2ConvertorMap; // 之前的方式，虚拟机重排序可能会发生问题

    ConverterFactory() {
        string2ConvertorMap = new HashMap<>();
        // 初始化映射表
        for (ParamType value : ParamType.values()) {
            string2ConvertorMap.put(value.getType(), value.getConvertor());
        }
    }

    public static ConverterFactory getInstance() {
        return INSTANCE;
    }


    /**
     * 通过分析器得到的参数类型, 获取对应的converter
     *
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
