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
        context.setToken(sb.toString());
        context.setLen(len);
    }
}
