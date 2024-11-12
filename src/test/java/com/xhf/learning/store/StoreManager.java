package com.xhf.learning.store;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xhf.learning.store.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.GsonUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class StoreManager {
    private String filePath;

    public StoreManager() {
        // load data
        // String filePath = AppSettings.getInstance().getFilePath() + "app.properties";
        this.filePath = "E:\\java_code\\leetcode-runner\\src\\main\\resources\\app.properties";
        this.loadCache();
    }

    public static class StoreContent {
        private String contentJson;
        private long expireTimestamp;

        public String getContentJson() {
            return contentJson;
        }

        public long getExpireTimestamp() {
            return expireTimestamp;
        }

        public void setContentJson(String contentJson) {
            this.contentJson = contentJson;
        }

        public void setExpireTimestamp(long expireTimestamp) {
            this.expireTimestamp = expireTimestamp;
        }
    }

    public static final String LEETCODE_SESSION_KEY = "LEETCODE_SESSION_KEY";

    public static final String QUESTION_LIST_KEY = "QUESTION_LIST_KEY";

    private final static Cache<String, StoreContent> cache = CacheBuilder.newBuilder().build();

    // require persistent cache
    private final static Cache<String, StoreContent> durableCache = CacheBuilder.newBuilder().build();

    private static volatile StoreManager instance;

    public static StoreManager getInstance() {
        if (instance != null) return instance;
        synchronized (StoreManager.class) {
            if (instance == null) {
                instance = new StoreManager();
            }
        }
        return instance;
    }

    public void addCache(String key, Object o, boolean isDurable) {
        addCache(key, GsonUtils.toJsonStr(o), isDurable, -1, null);
    }

    public void addCache(String key, Object o) {
        addCache(key, GsonUtils.toJsonStr(o), true, -1, null);
    }

    public void addCache(String key, String o, boolean isDurable, int expire, TimeUnit timeUnit) {
        StoreContent c = new StoreContent();
        c.setContentJson(o);
        if (expire != -1) {
            c.setExpireTimestamp(convertToTimestamp(expire, timeUnit));
        }else {
            c.setExpireTimestamp(-1);
        }

        if (isDurable) {
            durableCache.put(key, c);
        } else {
            cache.put(key, c);
        }
    }

    public String getCacheJson(String key) {
        StoreContent content = cache.getIfPresent(key);
        if (content != null) {
            return getIfNotExpireTime(content);
        }
        content = durableCache.getIfPresent(key);
        if (content != null) {
            return getIfNotExpireTime(content);
        }
        return null;
    }


    /**
     * get content and check whether it has expired
     * @param content
     * @return
     */
    private String getIfNotExpireTime(StoreContent content) {
        long expireTimestamp = content.getExpireTimestamp();
        if (expireTimestamp == -1) return content.getContentJson();
        return System.currentTimeMillis() < expireTimestamp ? content.getContentJson() : null;
    }

    private long convertToTimestamp(int expire, TimeUnit timeUnit) {
        long currentTimestamp = System.currentTimeMillis();

        long expireInMillis = timeUnit.toMillis(expire);

        return currentTimestamp + expireInMillis;
    }


    /*------------------------------disk---------------------------------*/
    private ReentrantLock lock = new ReentrantLock();

    public void persistCache() {
        // build properties
        Properties properties = new Properties();
        // lock the durableCache to avoid thread problems
        synchronized (durableCache) {
            durableCache.asMap().forEach((k, v) -> {
                properties.setProperty(k, GsonUtils.toJsonStr(v));
            });
        }
        // write to file
        try {
            FileUtils.writePropertiesFileContent(filePath, properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * load properties file to durable cache. if file not exists, load nothing
     */
    private void loadCache() {
        Properties properties = FileUtils.readPropertiesFileContent(filePath);
        properties.forEach((k, v) -> {
            durableCache.put((String) k, GsonUtils.fromJson((String) v, StoreContent.class));
        });
    }
}
