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
    private final Context ctx;

    public Segmentation(Reader reader) {
        // 设置默认大小
        sm = new SourceManager();
        sm.setSource(reader);
        itr = sm.iterator();
        pf = ProcessorFactory.getInstance();
        ctx = new Context(itr, sm);
    }

    public Segmentation(Reader reader, int bufferSize) {
        sm = new SourceManager(bufferSize);
        sm.setSource(reader);
        itr = sm.iterator();
        pf = ProcessorFactory.getInstance();
        ctx = new Context(itr, sm);
    }

    // 获取下一个分词
    public Token next() throws IOException {
        // ctx内还有剩余token没有完成处理
        if (ctx.hasToken()) {
            return ctx.getToken();
        }
        if (! sm.tryLoad()) return null;
        // 赋值上下文
        ctx.setC(itr.next());
        // 获取处理器
        Processor processor = pf.createProcessor(ctx);
        // 处理字符
        processor.doProcess(ctx);
        return ctx.getToken();
    }
}
