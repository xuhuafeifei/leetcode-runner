package com.xhf.leetcode.plugin.debug.command.parameter;

import com.xhf.leetcode.plugin.search.utils.CharacterHelper;

/**
 * r b 3 -> 3
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RBParameterExtractor implements ParameterExtractor {

    @Override
    public String extract(String inst) {
        inst = inst.trim();
        StringBuilder sb = new StringBuilder();
        int len = inst.length();
        for (int i = len - 1; i >= 0; i--) {
            if (CharacterHelper.isArabicNumber(inst.charAt(i))) {
                sb.append(inst.charAt(i));
            }
        }
        return sb.reverse().toString();
    }
}
