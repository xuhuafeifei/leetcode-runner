package com.xhf.leetcode.plugin.debug.execute.cpp.gdb;

import com.google.gson.internal.LinkedTreeMap;
import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;

/**
 * gdb object, 存储GDB-MI输出的对象, 可以通过key-value的形式获取
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class GdbObject extends GdbElement {
    private final LinkedTreeMap<String, GdbElement> members = new LinkedTreeMap<>(false);

    public void add(String key, GdbElement value) {
        members.put(key, value);
    }

    public GdbElement get(String key) {
        return members.get(key);
    }

    @Override
    public void add(GdbElement ele) {
        throw new NotSupportedException("GdbObject not support add(GdbElement)");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        members.forEach((k, v) -> sb.append(k).append(":").append(v.toString()));
        return sb.append("}").toString();

    }
}
