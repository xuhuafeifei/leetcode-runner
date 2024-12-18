package com.xhf.leetcode.plugin.debug.params;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class InstParserImpl implements InstParser{
    @Override
    public Instrument parse(String inst) {
        if (inst == null || inst.trim().isEmpty()) {
            return null;
        }

        inst = inst.trim();

        // 遍历每个 Operation 枚举值，检查是否匹配
        for (Operation operation : Operation.values()) {
            if (operation.matches(inst)) {
                // 提取指令的参数
                String param = operation.getParameterExtractor().extract(inst);
                return new Instrument(operation, param);
            }
        }

        // 如果没有匹配成功，则抛出异常
        return null;
    }
}
