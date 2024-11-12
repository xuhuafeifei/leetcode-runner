package com.xhf.leetcode.plugin.model;

public class SearchParams {
    private String categorySlug;
    private Integer skip;
    private Integer limit;
    private String searchKeywords;

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
        private String searchKeywords;

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
            params.setSearchKeywords(searchKeywords);
            return params;
        }
    }
}
