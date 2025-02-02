package com.xhf.leetcode.plugin.debug.execute.cpp.gdb;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class GdbElement {

    public boolean isGdbArray() {
        return this instanceof GdbArray;
    }

    public boolean isGdbObject() {
        return this instanceof GdbObject;
    }

    public boolean isGdbPrimitive() {
        return this instanceof GdbPrimitive;
    }

    public GdbPrimitive getAsGdbPrimitive() {
        if (isGdbPrimitive()) {
            return (GdbPrimitive) this;
        }
        throw new IllegalStateException("Not a Gdb Primitive : " + this);
    }

    public GdbArray getAsGdbArray() {
        if (isGdbArray()) {
            return (GdbArray) this;
        }
        throw new IllegalStateException("Not a Gdb Array : " + this);
    }

    public GdbObject getAsGdbObject() {
        if (isGdbObject()) {
            return (GdbObject) this;
        }
        throw new IllegalStateException("Not a Gdb Object : " + this);
    }

    public abstract void add(GdbElement value);

    public abstract String toString();
}
