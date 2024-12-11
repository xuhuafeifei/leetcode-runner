package com.xhf.leetcode.plugin.search;

import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;


/**
 * 数据源管理者, 负责从数据源分段加载数据. 内部通过{@link SourceBuffer}维护分段数据
 * <p>
 * 此外, {@link SourceManager}提供两个增强迭代器: {@link #itrAdvance}和{@link #captureIterator}
 * 并引入预加载机制, 维护{@link #preBuffer}, 解决中文文本分析可能存在的问题, 详见{@link PreBuffer}.
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SourceManager {
    /**
     * 缓冲区大小
     */
    public final static int DEFAULT_BUFFER_SIZE = 1024 * 2;
    /**
     * 缓冲区大小
     */
    private final int bufferSize;
    /**
     * 已加载数据偏移量
     */
    private int loadOffset;
    /**
     * 数据源
     */
    private Reader source;
    /**
     * 分段数据源加载缓冲区
     */
    private final SourceBuffer sourceBuffer;
    /**
     * 增强迭代器, 详见{@link IteratorAdvance}
     */
    private final IteratorAdvance itrAdvance;
    /**
     * 预加载缓冲区, 详见{@link PreBuffer}
     */
    private final PreBuffer preBuffer;
    /**
     * 快照迭代器
     */
    private final CaptureIterator captureIterator;

    public SourceManager() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public SourceManager(int bufferSize) {
        if (bufferSize < 10) {
            throw new IllegalArgumentException("bufferSize is too small!");
        }
        if (bufferSize > 1024 * 30) {
            throw new IllegalArgumentException("bufferSize is too large!");
        }
        this.bufferSize = bufferSize;
        sourceBuffer = new SourceBuffer(bufferSize);
        loadOffset = 0;
        itrAdvance = new IteratorAdvance(sourceBuffer.iterator());
        preBuffer = new PreBuffer(bufferSize);
        captureIterator = new CaptureIterator();
    }

    /**
     * 设置数据源后, 需要调用tryLoad方法加载数据源
     * <p>
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
     * <p>
     * 请注意, 该迭代器会在底层深度拷贝SourceBuffer的迭代器, 保留当前时刻底层迭代器的迭代进度
     * 因此请在快照迭代器创建前, 进行数据的加载(调用SourceManager.tryLoad())
     * <p>
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
        int read;
        if (preBuffer.isValid()) {
            read = segLoadFromSource(new CharArrayReader(preBuffer.getPreBuffer(), 0, preBuffer.getSize()));
            // 预加载缓冲区已被SourceBuffer加载, preBuffer设置为无效状态
            clearPreBuffer();
        } else {
            read = segLoadFromSource(source);
        }
        return read;
    }

    /**
     * 从数据源分段加载. 不允许在数据源迭代过程中调用该方法
     */
    private int segLoadFromSource(Reader source) throws IOException {
        if (source == null) {
            throw new NullPointerException("source is null, please set it first !");
        }
        int readCount = sourceBuffer.segLoad(source, 0, this.bufferSize);
        this.loadOffset += readCount;
        return readCount;
    }

    /**
     * 迭代器增强类, 用于增强SourceBuffer的迭代器
     * <p>
     * 增强迭代器在调用{@link #hasNext()}的时候, 如果没有下一个数据, 则尝试从数据源中加载数据, 进行迭代. 屏蔽消费者对分段数据的感知, 让消费者误以为提供的数据时连续的
     * 此外, 调用{@link #next()}方法时, 会对字符进行处理, 具体处理逻辑可参考{@link CharacterHelper}的regularize(char c)方法
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
                // 自动触发加载机制
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
            // 字符规则化
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
     * 快照迭代器, 负责迭代{@link #preBuffer}和{@link #sourceBuffer}数据, 同时还负责触发{@link SourceManager}(简称SM)的预加载机制
     * 该类不会影响{@link #sourceBuffer}(简称sB)对底层分段数据的迭代消费, 只对sB底层的迭代器做深拷贝处理
     * <p>
     * 如果在快照迭代过程中, {@link #itr}处理到{@link #sourceBuffer}的边界, 则会触发SM的预加载机制. 其原理是将{@link #source}内的数据读入{@link #preBuffer},
     * 然后{@link CaptureIterator}对{@link #preBuffer}进行迭代, 从而规避对底层sB内数据的修改
     * <p>
     * 详细的预加载机制和其引入的原理, 可参考{@link PreBuffer}
     * <p>
     * 值得注意的是, 当SM将{@link #source}数据加载到preBuffer中时, source内读取文件的指针则会偏移, 因此
     * 在SourceBuffer加载数据时, 需要从preBuffer中加载被预加载的那一部分数据. 当preBuffer中的数据读入sourceBuffer中后
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
         * 获取{@link CaptureIterator}(简称ci), 对底层{@link #sourceBuffer}的迭代器拍摄快照
         * <p>
         * {@link #captureIterator}内部维护两个指针, 其一是指向sB, 并且和sB的迭代器拍摄快照时指向相同位置.
         * 另一个是指向preBuffer
         * <p>
         * 需要注意的是, 每次获取快照迭代器时都需要对{@link #sourceBuffer}(简称sB)的iterator进行快照拍摄
         * 此外, captureIterator需要进行状态重置, 调用reset()方法. 这非常重要. 如果不重置则预加载机制会发生错误.
         * <p>
         * eg:
         * 以下为例
         * <p>
         * <第一轮匹配, 以'中'为start>
         * <p>
         *      sourceBuffer           未加载
         * [, , , , , , , 中, 位]   [数, ' ', ......]
         *                @
         *                ^
         *                       *(preBuffer未加载, *指向-1)
         * <p>
         * 假设SourceBuffer的迭代器指向'中'(@指向的位置), 此时CNProcessor进行中文分词
         * CNProcessor获取ci(CaptureIterator, ci所在位置是^, *. ^是对sB的快照, *是ci本身维护的指针)
         * 在'中'的基础上迭代后续字符
         * <p>
         * [, , , , , , , 中, 位]   [数, ' ', ......]
         *                 @  ^
         *                       *
         * <p>
         * 当处理到'位'的时候, 发现ci内部维护的sourceBuffer的深拷贝迭代器位于分段数据边界(^所指的位置),
         * 此时触发预加载机制, 将下一段分段数据读入preBuffer中.
         * <p>
         *     sourceBuffer           未加载
         * [, , , , , , , 中, 位]   [数, ' ', ......]
         *                @   ^          *
         * 预加载完成后, ci继续迭代. ci本身的指针在preBuffer中迭代, 直到指向' '(*所指的位置), 遇到空格, 分词结束, 返回false
         * <p>
         * <第二轮匹配, 以'位'为start>
         *     sourceBuffer           preBuffer
         * [, , , , , , , 中, 位] [数, ' ', ......]
         *                    @
         *                    ^        *
         * CNProcessor获取快照迭代器. 如果不触发reset(), 则在第二轮匹配中, 会导致ci指向的是字符' '(*所在位置)
         * 而ci内部维护的sB的快照则指向'中'(^所在位置)
         * <p>
         * 此刻的迭代逻辑是, 先判断底层sB是否还有未迭代的数据, 如果有, 则直接返回true, 否则尝试迭代preBuffer
         * 当前案例, sB的数据已经被ci维护的深拷贝迭代器消费完毕. 需要迭代preBuffer. 但在获取ci时，没有调用reset()方法
         * 导致ci自己的迭代器指向的时' '(*所在位置), 因此'数'将会被跳过, 从而导致数据的丢失. 因此, 获取ci时必须调用
         * reset()方法, 归位ci迭代器
         *
         */
        public void updateCapture() {
            itr = sourceBuffer.iterator().deepcopy();
            /*
                必须调用reset(), 详细原因可参考本方法注释
             */
            reset();
        }

        /**
         * 优先判断{@link #sourceBuffer}内的数据是否完成迭代, 然后迭代preBuffer.
         * 如果preBuffer中没有数据且sB数据已经迭代完成, 则触发预加载机制
         */
        @Override
        public boolean hasNext() {
            // 检查底层sourceBuffer内是否还有未迭代的数据
            boolean flag = itr.hasNext();
            if (flag) {
                return true;
            }
            if (! preBuffer.isValid()) {
                // preBuffer内没有数据, 执行预加载逻辑
                int read = loadPreBuffer();
                return read != -1;
            } else if (this.cursor != this.size - 1) {
                // 判断preBuffer内是否还有未读取的数据
                return true;
            } else {
                // preBuffer内部的数据全被读取完毕
                /*
                  一般来说不会出现preBuffer内部数据被读取完毕.
                  因为消费者进行的是分词行为, 对于中文分词来说, 长度一般不会超过5个字符
                  <p>
                  再加上每次分段读取, sourceBuffer和preBuffer维护的都是一段bufferSize(默认长度为1024 * 2)长度
                  的数据, 其长度完全覆盖最大分词的长度.
                  因此在正常情况下是不会出现preBuffer数据读取完毕的情况.
                 */
                return false;
            }
        }

        /**
         * 为preBuffer加载数据
         * @return
         */
        private int loadPreBuffer() {
            /*
              不允许在preBuffer已经包含有数据且未进行消费时, 进行数据加载操作
             */
            if (preBuffer.isValid() && this.cursor != this.size - 1) {
                throw new RuntimeException("should not set new source data when capture iterator haven't" +
                        " iterate all data that has been loaded!");
            }
            try {
                int read = source.read(preBuffer.getPreBuffer(), 0, bufferSize);
                this.size = read;
                if (read == -1) {
                    // 啥也没读到, 数据无了
                    preBuffer.setValid(false);
                }else {
                    preBuffer.setValid(true);
                }
                preBuffer.setSize(read);
                /*
                    preBuffer数据加载完毕, 重置captureIterator的迭代器
                 */
                this.reset();
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
            cursor = -1;
            size = preBuffer.getSize();
            // 暂时没想好capture iterator什么时候需要调用reset方法
            // throw new NotSupportedException("CaptureIterator not support reset !");
        }

        @Override
        public Iterator deepcopy() {
            throw new NotSupportedException("CaptureIterator not support deepcopy !");
        }
    }
}
