package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.Context;
import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.SourceManager;
import com.xhf.leetcode.plugin.search.dict.DictTree;
import com.xhf.leetcode.plugin.search.dict.Hit;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CNProcessor implements Processor {
    private final DictTree dt;

    private final SimpleHitStack hs;

    public CNProcessor() {
        dt = DictTree.getInstance();
        hs = new SimpleHitStack();
    }

    @Override
    public void doProcess(Context context) {
        hs.reset();
        SourceManager sm = context.getSm();
        Iterator captureItr = sm.captureIterator();
        // 迭代获取
        char searchC = context.getC();
        Hit preHit = dt.match(searchC);
        // 如果未命中, 直接返回
        if (! preHit.isHit()) {
            hs.add(preHit);
            setContext(context );
            return;
        }
        // 迭代
        while (captureItr.hasNext() && preHit.isHit()) {
            hs.add(preHit);
            searchC = captureItr.next();
            Hit hit = dt.match(searchC, preHit);
            preHit = hit;
        }
        // 获取最后一个是结尾的Hit
        while (hs.getTotalSize() != 1 && ! hs.peek().isEnd()) {
            hs.pop();
        }
        // 返回Token
        setContext(context);
        // 消费迭代器内的数据
        // consumeItr(sm);
    }

    private void consumeItr(SourceManager sm) {
        Iterator itr = sm.iterator();
        for (int i = 0; i < hs.getTotalSize() - 1; ++i) {
            itr.hasNext();
            itr.next();
        }
    }

    private void setContext(Context context) {
        context.setToken(hs.getToken());
        context.setLen(hs.getTotalSize());
    }

    public static class SimpleHitStack {
        private final Hit[] stack;
        private int top;
        private final int size = 15;
        private final StringBuilder sb;
        public SimpleHitStack() {
            stack = new Hit[size];
            top = -1;
            sb = new StringBuilder();
        }

        public boolean full() {
            return top == size - 1;
        }

        public boolean empty() {
            return top == -1;
        }

        public void add(Hit hit) {
            if (full())
                throw new RuntimeException("stack is full !");
            top += 1;
            stack[top] = hit;
        }

        public int getTotalSize() {
            return top + 1;
        }

        public Hit pop() {
            if (empty())
                throw new RuntimeException("stack is empty !");
            Hit res = stack[top];
            top -= 1;
            return res;
        }

        public Hit peek() {
            if (empty())
                throw new RuntimeException("stack is empty !");
            Hit res = stack[top];
            return res;
        }

        public void reset() {
            top = -1;
        }

        public String getToken() {
            sb.delete(0, sb.length());
            for (int i = 0; i <= top; ++i) {
                sb.append(stack[i].getC()) ;
            }
            return sb.toString();
        }
    }
}
