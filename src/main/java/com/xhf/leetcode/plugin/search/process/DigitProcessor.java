package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.Context;
import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DigitProcessor implements Processor {
    private final StringBuilder sb;

    public DigitProcessor() {
        sb = new StringBuilder();
    }

    @Override
    public void doProcess(Context context) {
        sb.delete(0, sb.length());

        char c = context.getC();
        int len = 1;
        Iterator itr = context.getIterator();
        sb.append(c);

        // 寻找最长数字
        while (itr.hasNext()) {
            // 查看下一个字符
            char next = itr.peekNext();
            // 依然是数字
            if (CharacterHelper.isArabicNumber(next)) {
                // 添加
                sb.append(next);
                // 消费数据
                itr.next();
                len += 1;
            } else {
                break;
            }
        }
        // 枚举连续字串
        enumerateSubstrings(sb, context);
        // context.addToken(sb.toString(), len);
    }

    /**
     * 枚举给定 StringBuilder 中的所有连续子串，并直接添加到 Context 中。
     * 考虑到该方法会被频繁调用, 因此内部采用StringBuilder进行优化
     *
     * @param sb   包含要处理的数字字符串的 StringBuilder
     * @param ctx  上下文对象，用于添加生成的标记
     */
    public void enumerateSubstrings(StringBuilder sb, Context ctx) {
        int length = sb.length();
        if (length == 1) {
            ctx.addToken(sb.toString(), length);
            return;
        }
        // 使用一个可变的 StringBuilder 来构建每个子串
        StringBuilder tokenBuilder = new StringBuilder();
        // 遍历所有可能的子串起始索引
        for (int start = 0; start < length; start++) {
            tokenBuilder.delete(0, tokenBuilder.length());
            // 遍历从当前起始索引到字符串末尾的所有可能结束索引
            for (int end = start; end < length; end++) {
                // 添加字符到当前构建的子串中
                tokenBuilder.append(sb.charAt(end));
                // 将构建好的子串添加到上下文中
                ctx.addToken(tokenBuilder.toString(), end - start + 1);
            }
        }
    }
}
