package com.xhf.leetcode.plugin.model;

import com.xhf.leetcode.plugin.utils.GsonUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * graphql request body
 *
 * @author feigebuge
 * @email 2508020102@qq.com
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

    /**
     * use search params to build graphql request body
     * especially for variable items
     * <p>
     * each field will be added to variables, and item's key is filedName, item's value is filedValue
     * <p>
     * additionally, if the field is not annotated with @Filters, it will be added to variables directly,
     * otherwise, it will be added to filters, and then will be added to variables
     *
     * @param params
     */
    public void setBySearchParams(SearchParams params) {
        Class aClass = params.getClass();
        HashMap<String, Object> filters = new HashMap<>();

        for (Field field : aClass.getDeclaredFields()) {
            // set accessible
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = null;
            try {
                value = field.get(params);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            // noting to add
            if (value == null) {
                continue;
            }
            if (field.getAnnotation(SearchParams.Filters.class) != null) {
                // add value to filters map
                filters.put(fieldName, value);
            } else {
                // add to variable
                addVariable(fieldName, value);
            }
        }
        // add filters
        addVariable("filters", filters);
    }

    public void clear() {
        this.variables.clear();
    }

    /**
     * search params, which will be used to search questions when a client executes graphql request
     * SearchParams will help user to build request body easily
     */
    public static class SearchParams {

        @Retention(RetentionPolicy.RUNTIME)
        public @interface Filters {

        }

        private String categorySlug;
        private Integer skip;
        private Integer limit;
        private String titleSlug;

        // filters content
        @Filters
        private String searchKeywords;


        public String getTitleSlug() {
            return titleSlug;
        }

        public void setTitleSlug(String titleSlug) {
            this.titleSlug = titleSlug;
        }

        public String getCategorySlug() {
            return categorySlug;
        }

        public void setCategorySlug(String categorySlug) {
            this.categorySlug = categorySlug;
        }

        public Integer getSkip() {
            return skip;
        }

        public void setSkip(Integer skip) {
            this.skip = skip;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getSearchKeywords() {
            return searchKeywords;
        }

        public void setSearchKeywords(String searchKeywords) {
            this.searchKeywords = searchKeywords;
        }

        public static class ParamsBuilder {
            private String categorySlug;
            private Integer skip;
            private Integer limit;
            private String titleSlug;

            // filters content
            private String searchKeywords;

            public ParamsBuilder setTitleSlug(String titleSlug) {
                this.titleSlug = titleSlug;
                return this;
            }

            public ParamsBuilder setCategorySlug(String categorySlug) {
                this.categorySlug = categorySlug;
                return this;
            }

            public ParamsBuilder setSkip(Integer skip) {
                this.skip = skip;
                return this;
            }

            public ParamsBuilder setLimit(Integer limit) {
                this.limit = limit;
                return this;
            }

            public ParamsBuilder setSearchKeywords(String searchKeywords) {
                this.searchKeywords = searchKeywords;
                return this;
            }

            public ParamsBuilder basicParams() {
                this.categorySlug = "all-code-essentials";
                this.skip = 0;
                this.limit = 4000;
                return this;
            }

            public SearchParams build() {
                SearchParams params = new SearchParams();
                params.setCategorySlug(categorySlug);
                params.setSkip(skip);
                params.setLimit(limit);
                params.setTitleSlug(titleSlug);
                params.setSearchKeywords(searchKeywords);
                return params;
            }
        }
    }
}
