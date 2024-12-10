package com.xhf.leetcode.plugin.window.filter;

/**
 * @param <T> 过滤的目标类型
 * @param <I> 过滤条件的类型
 */
public interface Filter<T, I> {
    /**
     * 执行过滤, 如果返回true, 则保留, 否则需要滤掉
     * @param t
     * @return
     */
    boolean doFilter(T t);
    Filter<T, I> addItem(I item);
    boolean contains(I item);
    boolean removeItem(I item);

    void removeAllItems();

    int itemCount();

    /**
     * 返回当前过滤器能够执行过滤行为
     * @return
     */
    boolean usable();
}
