package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.Context;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class NonProcessor implements Processor {
    /**
     * 当前processor处理无法识别的内容, 当遇到无法识别的字符时, 不做任何处理
     * @param context
     */
    @Override
    public void doProcess(Context context) {
    }
}
