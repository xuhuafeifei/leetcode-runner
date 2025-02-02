package com.xhf.leetcode.plugin.debug.execute.cpp.gdb;

import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class GdbPrimitive extends GdbElement {
    private Object value = null;

    public GdbPrimitive(String value) {
        this.value = value;
    }

    public GdbPrimitive(Number value) {
        this.value = value;
    }

    public GdbPrimitive(Boolean value) {
        this.value = value;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public boolean isNumber() {
        return value instanceof Number;
    }

    public boolean isString() {
        return value instanceof String;
    }

    public String getAsString() {
        return value.toString();
    }

    public Number getAsNumber() {
        return (Number) value;
    }

    public Boolean getAsBoolean() {
        return (Boolean) value;
    }

    @Override
    public void add(GdbElement parse) {
        throw new NotSupportedException("GdbPrimitive not support add(GdbElement)");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
