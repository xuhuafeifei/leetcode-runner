package com.xhf.leetcode.plugin.debug.execute.cpp.gdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * gdb array, 存储GDB-MI输出的数组
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class GdbArray extends GdbElement implements Iterable<GdbElement> {

    private final List<GdbElement> elements = new ArrayList<>();

    @NotNull
    @Override
    public Iterator<GdbElement> iterator() {
        return elements.iterator();
    }

    public void add(GdbElement element) {
        elements.add(element);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (GdbElement element : elements) {
            sb.append(element);
        }
        return sb.append("]").toString();
    }

    public GdbElement get(int idx) {
        if (idx >= elements.size()) {
            throw new IndexOutOfBoundsException("elements size = " + elements.size() + " idx = " + idx);
        }
        return elements.get(idx);
    }

    public int size() {
        return elements.size();
    }
}
