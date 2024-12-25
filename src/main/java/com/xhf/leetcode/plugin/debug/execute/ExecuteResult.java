package com.xhf.leetcode.plugin.debug.execute;

import com.google.gson.annotations.SerializedName;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.java.Context;

/**
 * 执行结果
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ExecuteResult {
    private Operation operation;
    /**
     * 为了配合Operation.NULL的引入, 增加moreInfo字段. 用于表示额外信息
     * ---------------------
     * 现在不配合Operation.NULL, moreInfo服务于捕获debug代码的std out / std error
     */
    @SerializedName("more_info")
    private String moreInfo;
    @SerializedName("success")
    private boolean success;
    @SerializedName("has_result")
    private boolean hasResult;
    /**
     * 输出结果
     */
    @SerializedName("result")
    private String result;
    /**
     * 错误信息
     */
    @SerializedName("msg")
    private String msg;
    /**
     * 添加的断点行数, 专门服务于UIOutput
     * UIOutput将会取消显示该断点, 因为该断点非法
     */
    @SerializedName("add_line")
    private int addLine;
    /**
     * 当前执行的方法名词
     */
    @SerializedName("method_name")
    private String methodName;
    /**
     * 上下文对象
     */
    private ExecuteContext context;
    @SerializedName("class_name")
    private String className;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isHasResult() {
        return hasResult;
    }

    public void setHasResult(boolean hasResult) {
        this.hasResult = hasResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
        this.hasResult = true;
    }

    public static ExecuteResult success(Operation operation, String result) {
        ExecuteResult r = new ExecuteResult();
        r.operation = operation;
        r.success = true;
        r.hasResult = true;
        r.result = result;
        return r;
    }

    public static ExecuteResult success(Operation operation) {
        ExecuteResult r = new ExecuteResult();
        r.operation = operation;
        r.success = true;
        r.hasResult = false;
        return r;
    }

    public static ExecuteResult fail(Operation operation) {
        ExecuteResult r = new ExecuteResult();
        r.operation = operation;
        r.success = false;
        r.msg = "Some error happens!";
        return r;
    }

    public static ExecuteResult fail(Operation operation, String msg) {
        ExecuteResult r = new ExecuteResult();
        r.operation = operation;
        r.success = false;
        r.msg = msg;
        return r;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public int getAddLine() {
        return addLine;
    }

    public void setAddLine(int addLine) {
        this.addLine = addLine;
    }

    @Override
    public String toString() {
        return "ExecuteResult{" +
                "operation=" + operation +
                ", success=" + success +
                ", hasResult=" + hasResult +
                ", result='" + result + '\'' +
                ", msg='" + msg + '\'' +
                ", addLine=" + addLine +
                '}';
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setContext(ExecuteContext context) {
        this.context = context;
    }

    public ExecuteContext getContext() {
        return context;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public String getMoreInfo() {
        return moreInfo;
    }
}
