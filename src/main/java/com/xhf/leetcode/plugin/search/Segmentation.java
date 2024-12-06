package com.xhf.leetcode.plugin.search;

import com.xhf.leetcode.plugin.search.process.Processor;
import com.xhf.leetcode.plugin.search.process.ProcessorFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * 分词处理器, 获取下一个分词
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Segmentation {
    private final SourceManager sm;
    private final Iterator itr;
    private final ProcessorFactory pf;

    public Segmentation(Reader reader) {
        sm = new SourceManager();
        sm.setSource(reader);
        itr = sm.iterator();
        pf = ProcessorFactory.getInstance();
    }

    // 获取下一个分词
    public String next() throws IOException {
        if (! sm.tryLoad()) return null;
        // 创建上下文
        Context context = new Context(itr.next(), itr);
        // 获取处理器
        Processor processor = pf.createProcessor(context);
        // 处理字符
        processor.doProcess(context);
        return context.getToken();
    }
}
