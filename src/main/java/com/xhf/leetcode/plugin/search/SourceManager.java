package com.xhf.leetcode.plugin.search;

import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;


/**
 * 数据源管理者, 负责从数据源分段加载数据
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SourceManager {
    public final static int BUFFER_SIZE = 1024;
    // 缓冲区大小
    public final static char[] DEFAULT_PRE_BUFFER = new char[BUFFER_SIZE];
    // 已加载数据偏移量
    private int loadOffset;
    // 数据源
    private Reader source;
    // 分段数据源加载缓冲区
    private final SourceBuffer sourceBuffer;
    // 增强迭代器
    private final Iterator itrAdvance;

    private final PreBuffer preBuffer;

    // 快照迭代器
    private CaptureIterator captureIterator;

    public SourceManager() {
        sourceBuffer = new SourceBuffer(BUFFER_SIZE);
        loadOffset = 0;
        itrAdvance = new IteratorAdvance(sourceBuffer.iterator());
        preBuffer = new PreBuffer();
        captureIterator = new CaptureIterator();
    }

    /**
     * 设置数据源后, 需要调用tryLoad方法加载数据源
     *
     * 或者可以通过增强迭代器的方法, 增强迭代器在迭代数据的时候, 如果没有下一个数据,
     * 增强迭代器则会尝试从数据源中加载数据, 进行迭代
     */
    public void setSource(Reader source) {
        // 如果数据源为空, 则不可能存在'未处理完source数据的情况'
        if (this.source == null) {
            this.source = source;
            return;
        }
        if (itrAdvance.hasNext()) {
            throw new RuntimeException("should not set new source data when iterator haven't" +
                    " iterate all data that has been loaded!");
        }
        this.source = source;
    }

    public void setSource(String s) {
        this.setSource(new StringReader(s));
    }

    public Iterator iterator() {
        return itrAdvance;
    }

    /**
     * 获取快照迭代器
     *
     * 请注意, 该迭代器会在底层深度拷贝SourceBuffer的迭代器, 保留当前时刻底层迭代器的迭代进度
     * 因此请在快照迭代器创建前, 进行数据的加载(调用SourceManager.tryLoad())
     *
     * 如果当前SM并未加载数据, 那么创建的快照迭代器无法迭代数据, 哪怕后续SM在调用tryLoad()方法
     * 快照迭代器依然无法进行数据迭代, 因为快照开启的时候, 底层迭代器没有数据可以迭代
     *
     * @return
     */
    public Iterator captureIterator() {
        captureIterator.updateCapture();
        return captureIterator;
    }

    // 尝试加载数据源, 如果source中的数据已经全部加载并处理, 则返回false
    public boolean tryLoad() throws IOException {
        // 如果迭代器还有数据, 不加载
        Iterator itr = sourceBuffer.iterator();
        if (itr.hasNext()) return true;
        // 如果迭代器没有数据, 从数据源分段加载
        int readCount = loadData();
        // 如果从source中加载数据量为-1, 则表示全部数据已经加载并处理完毕
        return readCount != -1;
    }

    /**
     * 向底层维护分段数据的SourceBuffer添加数据, 如果preBuffer中存在有效数据, 优先加载preBuffer
     * <p>
     * 当preBuffer内的数据加载完毕, preBuffer设置为无效状态
     *
     * @throws IOException
     */
    private int loadData() throws IOException {
        // 优先加载与缓冲区, 如果没有则从source中加载分段数据
        int read = segLoadFromSource(! preBuffer.isValid() ? source : new CharArrayReader(preBuffer.getPreBuffer(), 0, preBuffer.getSize()));
        // 预加载缓冲区已被SourceBuffer加载, preBuffer设置为无效状态
        clearPreBuffer();
        return read;
    }

    /**
     * 从数据源分段加载. 不允许在数据源迭代过程中调用该方法
     */
    private int segLoadFromSource(Reader source) throws IOException {
        if (source == null) {
            throw new NullPointerException("source is null, please set it first !");
        }
        int readCount = sourceBuffer.segLoad(source, 0, BUFFER_SIZE);
        this.loadOffset += readCount;
        return readCount;
    }

    /**
     * 迭代器增强类, 用于增强SourceBuffer的迭代器
     * <p>
     * 需要注意的是, 增强迭代器是暴露给其他模块使用, 本模块切勿使用增强迭代器, 否则可能产生
     * 预料之外的错误
     *
     */
    private class IteratorAdvance implements Iterator {
        private final Iterator itr;

        public IteratorAdvance(Iterator itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            boolean flag = itr.hasNext();
            if (flag) return true;
            try {
                loadData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return itr.hasNext();
        }

        @Override
        public char next() {
            if (itr.peekNext() == CharacterHelper.NULL) {
                try {
                    loadData();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return CharacterHelper.regularize(itr.next());
        }

        @Override
        public char peekNext() {
            return itr.peekNext();
        }

        @Override
        public char current() {
            return itr.current();
        }

        @Override
        public void reset() {
            itr.reset();
        }

        @Override
        public Iterator deepcopy() {
            return null;
        }
    }

    private void clearPreBuffer() {
        preBuffer.setValid(false);
    }


    /**
     * 迭代快照, 负责迭代SourceBuffer和PreBuffer, 该类不会影响SourceBuffer对底层数据的迭代消费, 只对底层SourceBuffer的迭代器做深拷贝处理
     * <p>
     * 如果在快照迭代过程中, itr处理到SourceBuffer.buffer的边界, 则会触发SourceManager(简称SM)的预加载机制. 其原理是将source内的数据读入SM的preBuffer,
     * 然后CaptureIterator对preBuffer进行迭代, 从而规避对底层SourceBuffer内数据的修改
     * <p>
     * 值得注意的是, 当SourceManager将source数据加载到preBuffer中时, source内读取文件的指针则会偏移, 因此
     * 在SourceBuffer加载数据时, 需要从preBuffer中加载被预加载的那一部分数据. 当preBuffer中的数据读入SourceBuffer中后
     * SourceManager则会修改preBuffer中的数据状态, 设置为无效数据
     *
     */
    private class CaptureIterator implements Iterator {
        /*-------SourceBuffer的迭代器的深拷贝对象--------*/
        private Iterator itr;

        /*---- 指向SM的preBuffer ----*/
        private int cursor;
        private int size;

        public CaptureIterator() {
            updateCapture();
            cursor = -1;
            size = 0;
        }

        /**
         * 更新底层SourceBuffer迭代器的快照
         */
        public void updateCapture() {
            itr = sourceBuffer.iterator().deepcopy();
        }

        /**
         * 优先判断SourceBuffer内的数据是否完成迭代
         * 然后检查preBuffer, 如果preBuffer = DEFAULT_PRE_BUFFER, 则尝试导入数据
         * 如果preBuffer != DEFAULT_PRE_BUFFER, 迭代preBuffer
         */
        @Override
        public boolean hasNext() {
            // 检查底层SourceBuffer内是否还有未迭代的数据
            boolean flag = itr.hasNext();
            if (flag) return true;
            // 判断preBuffer内是否还有未读取的数据
            if (! preBuffer.isValid()) {
                int read = loadPreBuffer();
                return read != -1;
            } else if (this.cursor != this.size - 1) {
                return true;
            } else {
                // preBuffer内部的数据全被读取完毕
                return false;
            }
        }

        /**
         * 为preBuffer加载数据
         * @return
         */
        private int loadPreBuffer() {
            try {
                int read = source.read(preBuffer.getPreBuffer(), 0, BUFFER_SIZE);
                this.size = read;
                if (read != -1) {
                    preBuffer.setValid(true);
                }else {
                    preBuffer.setValid(false);
                }
                preBuffer.setSize(read);
                return read;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 优先使用SourceBuffer的深拷贝迭代器进行迭代, 如果没有数据, 迭代preBuffer
         */
        @Override
        public char next() {
            boolean flag = itr.hasNext();
            if (flag) {
                return itr.next();
            }
            // 迭代preBuffer
            int i = this.cursor + 1;
            if (i >= this.size)
                throw new NoSuchElementException();
            this.cursor += 1;
            return preBuffer.getPreBuffer()[i];
        }

        @Override
        public char peekNext() {
            int i = this.cursor + 1;
            if (i >= this.size)
                throw new NoSuchElementException();
            return preBuffer.getPreBuffer()[i];
        }

        @Override
        public char current() {
            int i = this.cursor;
            // i == -1, 表示preBuffer没有迭代
            if (i == -1) {
                return itr.current();
            }
            // 返回迭代到的preBuffer的数据信息
            return preBuffer.getPreBuffer()[i];
        }

        @Override
        public void reset() {
            // cursor = -1;
            // size = 0;
            // 暂时没想好capture iterator什么时候需要调用reset方法
            throw new NotSupportedException("CaptureIterator not support reset !");
        }

        @Override
        public Iterator deepcopy() {
            throw new NotSupportedException("CaptureIterator not support deepcopy !");
        }
    }
}
