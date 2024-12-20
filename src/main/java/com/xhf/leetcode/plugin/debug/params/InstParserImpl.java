package com.xhf.leetcode.plugin.debug.params;

import com.xhf.leetcode.plugin.debug.reader.ReadType;

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
    public Instrument parse(String command, ReadType readType) {
        if (command == null || command.trim().isEmpty()) {
            return null;
        }

        command = command.trim();

        // 遍历每个 Operation 枚举值，检查是否匹配
        for (Operation operation : Operation.values()) {
            if (operation.matches(command)) {
                // 提取指令的参数
                String param = operation.getParameterExtractor().extract(command);
                return Instrument.success(readType, operation, param);
            }
        }

        // 如果没有匹配成功，则抛出异常
        return null;
    }
}
