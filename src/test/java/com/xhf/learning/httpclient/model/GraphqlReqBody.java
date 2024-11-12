package com.xhf.learning.httpclient.model;

import com.xhf.leetcode.plugin.utils.GsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * graphql请求体
 */
public class GraphqlReqBody {
    private String query;
    private final Map<String, Object> variables = new HashMap<>();
    private String operationNames = "";

    public GraphqlReqBody(String query) {
        this.query = query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void addVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    public void setOperationNames(String operationNames) {
        this.operationNames = operationNames;
    }

    public String toJsonStr() {
        return GsonUtils.toJsonStr(this);
    }
}
