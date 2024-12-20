package com.xhf.leetcode.plugin.debug.params;

import com.xhf.leetcode.plugin.debug.reader.ReadType;

/**
 * 指令不区分环境, 无论是Java亦或是python, 都接受同一套指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Instrument {
    /**
     * 标记当前指令是以何种方式读入
     */
    private ReadType readType;
    private boolean exit;
    private boolean success;
    private Operation operation;
    private String param;

    private Instrument(ReadType readType) {
        this.readType = readType;
    }

    public Instrument(ReadType readType, Operation operation, String param) {
        this.readType = readType;
        this.operation = operation;
        this.param = param;
    }

    public static Instrument success(ReadType readType, Operation operation, String param) {
        Instrument instrument = new Instrument(readType, operation, param);
        instrument.exit = false;
        instrument.success = true;
        return instrument;
    }

    public static Instrument quit(ReadType readType) {
        Instrument instrument = new Instrument(readType);
        instrument.success = true;
        instrument.exit = true;
        return instrument;
    }

    public static Instrument error(ReadType readType) {
        Instrument instrument = new Instrument(readType);
        instrument.success = false;
        instrument.exit = false;
        return instrument;
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

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ReadType getReadType() {
        return readType;
    }

    public void setReadType(ReadType readType) {
        this.readType = readType;
    }

    @Override
    public String toString() {
        return "Instrument{" +
                "readType=" + readType.getType() +
                ", exit=" + exit +
                ", success=" + success +
                ", operation=" + operation +
                ", param='" + param + '\'' +
                '}';
    }
}
