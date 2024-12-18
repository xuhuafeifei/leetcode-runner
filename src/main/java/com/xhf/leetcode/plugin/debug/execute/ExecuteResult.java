package com.xhf.leetcode.plugin.debug.execute;

/**
 * 执行结果
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ExecuteResult {
    private boolean success;
    private boolean hasResult;
    private String result;
    private String msg;

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
    }

    public static ExecuteResult success(String result) {
        ExecuteResult r = new ExecuteResult();
        r.success = true;
        r.hasResult = true;
        r.result = result;
        return r;
    }

    public static ExecuteResult success() {
        ExecuteResult r = new ExecuteResult();
        r.success = true;
        r.hasResult = false;
        return r;
    }

    public static ExecuteResult fail() {
        ExecuteResult r = new ExecuteResult();
        r.success = false;
        r.msg = "Some error happens!";
        return r;
    }

    public static ExecuteResult fail(String msg) {
        ExecuteResult r = new ExecuteResult();
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
}
