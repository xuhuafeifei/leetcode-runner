package com.xhf.leetcode.plugin.model;

import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public void setBySearchParams(SearchParams params) {
        if (StringUtils.isNotBlank(params.getCategorySlug())) {
            addVariable("categorySlug", params.getCategorySlug());
        }
        if (! Objects.isNull(params.getLimit())) {
            addVariable("limit", params.getLimit());
        }
        if (! Objects.isNull(params.getSkip())) {
            addVariable("skip", params.getSkip());
        }
        HashMap<String, Object> filters = new HashMap<>();
        if (! StringUtils.isNotBlank(params.getSearchKeywords())) {
            filters.put("searchKeywords", params.getSearchKeywords());
        }
        addVariable("filters", filters);
    }

    public void clear() {
        this.variables.clear();
    }
}
