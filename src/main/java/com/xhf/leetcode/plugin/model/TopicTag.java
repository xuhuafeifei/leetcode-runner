package com.xhf.leetcode.plugin.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TopicTag {
    private String id;
    private String name;
    private String slug;
    private String nameTranslated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getNameTranslated() {
        return nameTranslated;
    }

    public void setNameTranslated(String nameTranslated) {
        this.nameTranslated = nameTranslated;
    }
}
