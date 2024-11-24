package com.xhf.leetcode.plugin.model;

import org.apache.commons.lang.StringUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Solution {
    private String slug;
    private String title;
    private Integer hitCount;
    private Integer favoriteCount;
    private Author author;
    private String translatedTitle;
    private String translatedContent;
    private String summary;

    private static final String whiteSpace = "              ";

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (author.getUsername().equals("LeetCode-Solution")) {
            sb.append("☆ 官方：");
        }else {
            sb.append(whiteSpace);
        }
        sb.append("[").append(getName()).append("]").append("\n");
        sb.append(whiteSpace).append(title).append("\n");
        sb.append(whiteSpace).append(summary.replace("\n", "")).append("\n");
        sb.append(whiteSpace).append("🖱 ").append(hitCount).append(" ").append("❤ ").append(favoriteCount);
        return sb.toString();
    }

    private String getName() {
        try {
            String realName = author.getProfile().getRealName();
            if (StringUtils.isNotBlank(realName)) {
                return realName;
            }
        } catch (Exception ignored) {}
        return author.getUsername();
    }

    static class Author {
        private String username;
        private Profile profile;

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        static class Profile {
            private String userSlug;
            private String realName;

            public String getUserSlug() {
                return userSlug;
            }

            public void setUserSlug(String userSlug) {
                this.userSlug = userSlug;
            }

            public String getRealName() {
                return realName;
            }

            public void setRealName(String realName) {
                this.realName = realName;
            }
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
