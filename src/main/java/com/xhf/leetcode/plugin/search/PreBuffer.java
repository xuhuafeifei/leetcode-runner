package com.xhf.leetcode.plugin.search;

/**
 * 预加载缓冲区, 配合快照迭代器使用. PreBuffer(简称PB)维护preBuffer数组, 以及缓冲区内有效数据长度, PB的有效性
 * <p>
 * PB是为了解决capture iterator(简称ci)在迭代边界数据时, 进一步迭代数据遇到诸多问题而提出的解决方案
 * PB的出现允许ci在不修改底层SourceBuffer(简称SB)内部数据的情况下, 让ci具有迭代后续分段数据的能力
 * <p>
 * 与此同时, PB的出现也带来了全新的问题: source读取位置发生偏移. 这就导致PB提前加载了SB因该
 * 加载source的那部分分段数据. 因此在PB内部存在合法数据的情况下, SB因该先从PB内部读取数据
 * <p>
 *
 * 为了更好的解释PB引入的必要性, 需要先介绍业务背景:
 * <p>
 * 在进行中文分词处理时, 可能会遇到如下情况:
 * 假设下方字符数组是PB维护的分段数据
 * [一, 如, 既, 往, a]
 *  ^
 * 当前SB迭代器处理到第一个'一'字, 按照最长匹配原则, SB会持续迭代后续字符, 直到遇到'a', 则会终止匹配
 * 最终以'一'为start, 匹配到的最长合法CN_Token(中文token)是'一如既往'
 * <p>
 * 但如果字符'a'提前出现, 则会产生其它问题, 比如下方情况:
 * [一, 如, a, 既, 往]
 *  ^
 * 按照最长匹配原则, SB会迭代到下标为2处, 也就是'a'. 此时迭代情况如下所示:
 * [一, 如, a, 既, 往]
 *         ^
 * 当迭代到'a'后, '一如a'不可能进一步匹配出更长的字符, 因此终止匹配. 以'一'为start的最长token是'一如', 但'一如'不是合法的词组,
 * 以'一'为start的CN_Token是'一'. 此时就因该终止最长匹配. 然后从'如'字为start进行下一轮的最长匹配
 * <p>
 * 但是'如'字已经被错误的消费, 当前迭代器不支持rollback操作, 因此'如'字被永远的过滤.
 * <p>
 * 上述问题产生的原因是: 中文字符存在语境, 当多个字符组合在一起可能组成非法CN_Token. 因此在迭代的时候不能真的进行数据消费.
 * 只有在完成匹配后, 才能进行数据消费.
 * <p>
 * 为了解决上述问题, SourceManager(简称SM)引入快照迭代器(CaptureIterator, 简称ci), 通过迭代快照的形式, 避免SB把原始数据消费.
 * 需要注意的是, 为了保证效率, 此处的快照只是对SB迭代器的快照, 而不是对SB本身的快照. 相当于SB被两个迭代器迭代, 一个是
 * SB内部的iterator, 另一个是SM的ci
 * <p>
 *
 * 然而CaptureIterator(简称ci)并不能解决所有问题, 当遇到下述情况依然存在问题:
 * 假设下方是source中的所有数据, SB一次性只能加载2个字符的分段数据
 * [一, 如], [a, 既] [往]
 *      ^
 * 目前SB维护的分段数据是[一, 如], 并且迭代到'如'字. 按照最长匹配原则, 此时'一如'可能匹配出更长的CN_Token,
 * 因此匹配继续. 然而SB加载的分段数据已经全部加载完毕, 需要重新加载数据. 一旦执行加载操作, SB维护的[一, 如]则会被
 * [a, 既]覆盖, '一如a'是非法CN_Token, 需要终止匹配, 迭代器需要重新从'如'字开始匹配. 然而SB数据被覆盖
 * source内部维护的读取偏移量也被改变, '如'字永久丢失
 * <p>
 * 为了解决上述问题, 引入PB, 快照迭代器迭代的过程中, 会将分段数据导入PB内, 以此保证SB内部数据不会被覆盖
 * 如果SB需要读取source中下一段数据, 则会优先检查PB内部是否有合法数据. 如果有则会优先加载PB, 加载完毕后设置PB
 * 数据为非法数据, 以此表示PB数据已被加载
 * <p>
 * 另外, PB内部维护的buffer大小和SB内部维护的buffer大小一致
 */
class PreBuffer {
    private char[] preBuffer;
    // preBuffer是否有效, 该值有效时, 数据才会加载到SourceBuffer内
    private boolean preBufferValid;
    private int size;

    public PreBuffer() {
        init();
    }

    public void init() {
        preBuffer = SourceManager.DEFAULT_PRE_BUFFER;
        preBufferValid = false;
    }

    public boolean isValid() {
        return preBufferValid;
    }

    public void setValid(boolean flag) {
        this.preBufferValid = flag;
    }

    public char[] getPreBuffer() {
        return preBuffer;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int read) {
        this.size = read;
    }
}