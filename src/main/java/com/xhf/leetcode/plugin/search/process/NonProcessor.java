package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.Context;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class NonProcessor implements Processor {
    /**
     * 当前processor处理无法识别的内容. 处理逻辑为持续迭代context数据, 直到迭代出能够识别的字符, 如果没有额外数据, 则会终止迭代
     * @param context
     */
    @Override
    public void doProcess(Context context) {
        ProcessorFactory pf = ProcessorFactory.getInstance();
        while (context.hasNext()) {
            context.nextC();
            Processor processor = pf.createProcessor(context);
            // 如果下一个字符数据能被工厂识别, 则进行处理
            if (! (processor instanceof NonProcessor) ) {
                processor.doProcess(context);
                // 完成处理, 终止运行
                return;
            }
        }
    }
}
