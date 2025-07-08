package com.xhf.leetcode.plugin.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Submission {

    private String id;
    private String status;
    private String statusDisplay;
    private String lang;
    private String runtime;
    private String memory;

    private String code;
    private String timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String convertTime() {
        long timestamp = Long.parseLong(getTimestamp());
        Instant instant = Instant.ofEpochSecond(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int[] widths = new int[]{25, 15, 10, 10, 20};
        String[] contents = new String[]{getStatusDisplay(), getLang(), getRuntime(), getMemory(), convertTime()};
        int len = 0;
        for (int i = 0; i < widths.length; i++) {
            len += widths[i];
            sb.append(contents[i]);
            while (sb.length() < len) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public boolean isAc() {
        return getStatus().equals("AC");
    }
}
