package com.xhf.leetcode.plugin.search;

import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;

/**
 * 数据源缓冲区, 用于维护从数据源加载到的分段数据
 * <p>
 * {@link SourceBuffer}通过迭代器返回正在被消费的数据, 如果迭代器将所有数据迭代完成, 则表示SourceBuffer所有的数据均
 * 被消费者处理完毕
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SourceBuffer {

    /**
     * 缓冲区
     */
    private final char[] buffer;
    /**
     * 迭代器, 负责迭代buffer
     */
    private final Itr it;
    /**
     * 统计缓冲区内有效数据的数量, 如果为-1, 则表示缓冲区还未被加载
     */
    private int totalCnt;

    public SourceBuffer() {
        this(SourceManager.DEFAULT_BUFFER_SIZE);
    }

    public SourceBuffer(int BUFFER_SIZE) {
        this.buffer = new char[BUFFER_SIZE];
        this.totalCnt = -1;
        this.it = new Itr();
    }

    public int loadFromString(String s) {
        StringReader reader = new StringReader(s);
        try {
            return segLoad(reader, 0, buffer.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 分段加载数据. 同时重置迭代器状态
     * <p>
     * 需要注意的是, SourceBuffer不允许在数据迭代过程中调用该方法, 否则会在Itr内部重置时抛出异常
     *
     * @param source 数据源, 分段数据的来源. SourceBuffer从source中分段加载数据
     * @param offset 详见{@link Reader}的read方法.
     * @param length 详见{@link Reader}的read方法.
     */
    public int segLoad(Reader source, int offset, int length) throws IOException {
        if (length > buffer.length) {
            throw new IllegalArgumentException("length cannot be greater than buffer length, source buffer length is "
                + buffer.length + " your support length is " + length);
        }
        if (offset > buffer.length) {
            throw new IllegalArgumentException("offset cannot be greater than buffer length, buffer length is= "
                + buffer.length + " but your support offset is " + offset);
        }
        int read = source.read(buffer, offset, length);
        this.totalCnt = read;
        // 数据更新, 需要重置迭代器
        it.reset();
        return read;
    }

    public Iterator iterator() {
        return it;
    }

    public String getBufferStr(int start, int length) {
        return new String(buffer, start, length);
    }

    public String getBufferStr() {
        return getBufferStr(0, buffer.length);
    }

    /**
     * 迭代器. 维护分段数据{@link #buffer}消费情况. 被迭代过的数据, 会被标记为已消费
     */
    private class Itr implements Iterator {

        /**
         * 当前遍历到的元素下标
         */
        private int cursor;
        /**
         * 缓冲区有效数据的数量
         */
        private int size;

        public Itr() {
            this.cursor = -1;
            this.size = totalCnt;
        }

        @Override
        public boolean hasNext() {
            return this.size != -1 && this.cursor != this.size - 1;
        }

        @Override
        public char next() {
            int i = this.cursor + 1;
            if (i >= this.size) {
                throw new NoSuchElementException();
            }
            this.cursor += 1;
            return buffer[i];
        }

        @Override
        public char peekNext() {
            int i = this.cursor + 1;
            if (i >= this.size) {
                return CharacterHelper.NULL;
            }
            return buffer[i];
        }

        @Override
        public char current() {
            int i = this.cursor;
            if (i == -1) {
                throw new NoSuchElementException();
            }
            return buffer[i];
        }

        @Override
        public void reset() throws RuntimeException {
            // 如果cursor处于迭代过程中, buffer还有数据没有完成迭代, 则不允许执行reset操作
            if (hasNext()) {
                throw new RuntimeException("The reset method should not be executed when " +
                    "the iterator has not completed iterating over all the data.");
            }
            this.cursor = -1;
            this.size = totalCnt;
        }

        @Override
        public Iterator deepcopy() {
            Itr itr = new Itr();
            itr.size = this.size;
            itr.cursor = this.cursor;
            return itr;
        }
    }
}
