package com.xhf.leetcode.plugin.debug.params;

/**
 * 指令不区分环境, 无论是Java亦或是python, 都接受同一套指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Instrument {
    private Operation operation;
    private String param;

    public Instrument(Operation operation, String param) {
        this.operation = operation;
        this.param = param;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "Instrument{" +
                "operation=" + operation +
                ", param='" + param + '\'' +
                '}';
    }
}
