package com.xhf.leetcode.plugin.search;

import java.io.IOException;
import java.io.Reader;


/**
 * 数据源管理者, 负责从数据源分段加载数据
 * 同时提供迭代下一段数据源的功能
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SourceManager {
    // 缓冲区大小
    public final static int BUFFER_SIZE = 1024;
    // 已加载数据偏移量
    private int loadOffset;
    // 数据源
    private Reader source;
    // 分段数据源加载缓冲区
    private final SourceBuffer sourceBuffer;

    public SourceManager() {
        sourceBuffer = new SourceBuffer(BUFFER_SIZE);
        loadOffset = 0;
    }

    public void setSource(Reader source) {
        this.source = source;
    }

    public Iterator iterator() {
        return sourceBuffer.iterator();
    }

    // 从数据源分段加载
    private int segLoadFromSource() throws IOException {
        if (source == null) {
            throw new NullPointerException("source is null, please set it first !");
        }
        int readCount = sourceBuffer.segLoad(source, 0, BUFFER_SIZE);
        this.loadOffset += readCount;
        return readCount;
    }

    // 尝试加载数据源, 如果source中的数据已经全部加载并处理, 则返回false
    public boolean tryLoad() throws IOException {
        // 如果迭代器还有数据, 不加载
        Iterator itr = sourceBuffer.iterator();
        if (itr.hasNext()) return true;
        // 如果迭代器没有数据, 从数据源分段加载
        int readCount = segLoadFromSource();
        // 如果从source中加载数据量为-1, 则表示全部数据已经加载并处理完毕
        return readCount != -1;
    }
}
