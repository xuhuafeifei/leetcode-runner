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
     * 使用{@link SearchParams}构建 GraphQL 请求体
     * 特别是针对variables变量项
     * <p>
     * 每个字段将被添加到variables中，键是 filedName，值是 filedValue
     * <p>
     * 此外，如果字段没有使用 @Filters 注解，则会直接添加到variables中，
     * 否则，会先将其添加到过filters中，然后再将filters添加到variables中
     *
     * @param params 搜索参数
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
        if (filters.isEmpty()) return;
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
        private String questionSlug;
        private Integer first;
        private String orderBy;

        public String getQuestionSlug() {
            return questionSlug;
        }

        public void setQuestionSlug(String questionSlug) {
            this.questionSlug = questionSlug;
        }

        public Integer getFirst() {
            return first;
        }

        public void setFirst(Integer first) {
            this.first = first;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

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

            private String questionSlug;
            private Integer first;
            private String orderBy;

            public ParamsBuilder setQuestionSlug(String questionSlug) {
                this.questionSlug = questionSlug;
                return this;
            }
            public ParamsBuilder setFirst(Integer first) {
                this.first = first;
                return this;
            }

            public ParamsBuilder setOrderBy(String orderBy) {
                this.orderBy = orderBy;
                return this;
            }

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
                this.limit = 40;
                return this;
            }

            public SearchParams build() {
                SearchParams params = new SearchParams();
                params.setCategorySlug(categorySlug);
                params.setSkip(skip);
                params.setLimit(limit);
                params.setTitleSlug(titleSlug);
                params.setSearchKeywords(searchKeywords);
                params.setFirst(first);
                params.setOrderBy(orderBy);
                params.setQuestionSlug(questionSlug);
                return params;
            }
        }
    }
}
