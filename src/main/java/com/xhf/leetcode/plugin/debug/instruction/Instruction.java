package com.xhf.leetcode.plugin.debug.instruction;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.reader.ReadType;

/**
 * 指令不区分环境, 无论是Java亦或是python, 都接受同一套指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Instruction {
    /**
     * 标记当前指令是以何种方式读入
     */
    private ReadType readType;
    private boolean exit;
    private boolean success;
    private Operation operation;
    private String param;

    private Instruction(ReadType readType) {
        this.readType = readType;
    }

    public Instruction(ReadType readType, Operation operation, String param) {
        this.readType = readType;
        this.operation = operation;
        this.param = param;
    }

    public static Instruction success(ReadType readType, Operation operation, String param) {
        Instruction instruction = new Instruction(readType, operation, param);
        instruction.exit = false;
        instruction.success = true;
        return instruction;
    }

    public static Instruction quit(ReadType readType) {
        Instruction instruction = new Instruction(readType);
        instruction.success = true;
        instruction.exit = true;
        return instruction;
    }

    public static Instruction error(ReadType readType) {
        Instruction instruction = new Instruction(readType);
        instruction.success = false;
        instruction.exit = false;
        return instruction;
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
        return "Instruction{" +
                "readType=" + readType.getType() +
                ", exit=" + exit +
                ", success=" + success +
                ", operation=" + operation +
                ", param='" + param + '\'' +
                '}';
    }

    public Instruction copy() {
        // 深拷贝
        Instruction instruction = new Instruction(this.readType, this.operation, this.param);
        instruction.exit = this.exit;
        instruction.success = this.success;
        return instruction;
    }
}
