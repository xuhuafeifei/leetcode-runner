package com.xhf.leetcode.plugin.io.file;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * support cache ability and disk persistence
 * 如果发现durableCache发生变化, 立刻将数据异步写入磁盘(因为目前项目存储容量不大, 触发频率不算太高, 因此通过及时持久化来保存数据)
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@Service(Service.Level.PROJECT)
public final class StoreService implements Disposable {
    private Project project;

    public StoreService(Project project) {
        this.project = project;
        // load data
        /*
        this.filePath = new FileUtils.PathBuilder(
                                AppSettings.getInstance().getFilePath()
                             ).append(cacheFileName).build();
         */
        // this.filePath = AppSettings.getInstance().getCoreFilePath();
        // this.filePath = "E:\\java_code\\leetcode-runner\\src\\main\\resources\\app.properties";
        this.loadCache();
    }

    public static final String cacheFileName = "app.properties";

    public String getCacheFilePath() {
        String path = AppSettings.getInstance().getCoreFilePath();
        return new FileUtils.PathBuilder(path).append(cacheFileName).build();
    }

    public static StoreService getInstance(Project project) {
        return project.getService(StoreService.class);
    }

    @Override
    public void dispose() {
        this.persistCache();
    }

    public void clearCache() {
        cache.invalidateAll();
        durableCache.invalidateAll();
        FileUtils.deleteFile(getCacheFilePath());
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

    public static final String LEETCODE_TODAY_QUESTION_KEY = "LEETCODE_TODAY_QUESTION_KEY ";

    private final static Cache<String, StoreContent> cache = CacheBuilder.newBuilder().build();

    // require persistent cache
    private final static Cache<String, StoreContent> durableCache = CacheBuilder.newBuilder().build();

    public void addCache(String key, Object o, boolean isDurable) {
        addCache(key, GsonUtils.toJsonStr(o), isDurable, -1, null);
    }

    public void addCache(String key, Object o) {
        addCache(key, GsonUtils.toJsonStr(o), true, -1, null);
    }

    /**
     * 计数器
     */
    private int cnt = 0;
    private long last_time = 0;

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
            persistAndLog("新增缓存数据!", true);
        } else {
            cache.put(key, c);
        }
    }

    /**
     * 持久化数据并记录日志
     * @param info 日志信息, 统计缓存更新次数和频率
     * @param async 是否异步
     */
    private void persistAndLog(String info, boolean async) {
        frequentLog(info);
        this.persistCache(async);
    }

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5, // 核心线程数
            5, // 最大线程数
            1, // 线程存活时间
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), // 无界阻塞队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：CallerRunsPolicy
    );


    private void frequentLog(final String info) {
        executor.execute(() -> {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                if (cnt == 0) {
                    LogUtils.simpleDebug(info + " 第" + cnt + "次刷新");
                } else {
                    LogUtils.simpleDebug(info + " 第" + cnt + "次刷新, 与上次时间差为 = " + (now - last_time) + "ms");
                }
                cnt += 1;
                last_time = now;
            } finally {
                lock.unlock();
            }
        });
    }


    public boolean contains(String key) {
        return StringUtils.isNotBlank(getCacheJson(key));
    }

    /**
     * load content json
     * @param key
     * @return
     */
    public String getCacheJson(String key) {
        if (key == null) {
            return null;
        }
        StoreContent content = cache.getIfPresent(key);
        if (content != null) {
            return handleStoreContent(key, getIfNotExpireTime(content));
        }
        content = durableCache.getIfPresent(key);
        if (content != null) {
            return handleStoreContent(key, getIfNotExpireTime(content));
        }
        return null;
    }

    private String handleStoreContent(String key, String ifNotExpireTime) {
        if (ifNotExpireTime == null) {
            removeCache(key);
        }
        return ifNotExpireTime;
    }

    private void removeCache(String key) {
        durableCache.invalidate(key);
        persistAndLog("删除缓存数据!", true);
    }

    /**
     * load content and convert it into certain class
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getCache(String key, Class<T> clazz) {
        String cacheJson = getCacheJson(key);
        if (cacheJson == null) return null;
        return GsonUtils.fromJson(cacheJson, clazz);
    }


    /**
     * get content and check whether it has expired
     * @param content
     * @return
     */
    private String getIfNotExpireTime(StoreContent content) {
        long expireTimestamp = content.getExpireTimestamp();
        if (expireTimestamp == -1) {
            return content.getContentJson();
        }
        return System.currentTimeMillis() < expireTimestamp ? content.getContentJson() : null;
    }

    private long convertToTimestamp(int expire, TimeUnit timeUnit) {
        long currentTimestamp = System.currentTimeMillis();

        long expireInMillis = timeUnit.toMillis(expire);

        return currentTimestamp + expireInMillis;
    }


    /*------------------------------disk---------------------------------*/
    private ReentrantLock lock = new ReentrantLock();


    /**
     * persist cache to file
     * @param async true:异步, false:同步
     */
    private void persistCache(boolean async) {
        if (async) {
            executor.execute(this::persistCache);
        } else {
            this.persistCache();
        }
    }

    private void persistCache() {
        this.scanFileCache();
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
            FileUtils.writePropertiesFileContent(getCacheFilePath(), properties);
        } catch (IOException e) {
            System.err.println("write file error! filePath = " + getCacheFilePath());
        }
    }

    /**
     * load properties file to durable cache. if the file does not exist, load nothing
     */
    private void loadCache() {
        Properties properties = FileUtils.readPropertiesFileContent(getCacheFilePath());
        properties.forEach((k, v) -> {
            durableCache.put((String) k, GsonUtils.fromJson((String) v, StoreContent.class));
        });
    }

    /**
     * scan all file paths in the cache, and remove those deleted from the local file system
     */
    private void scanFileCache() {
        durableCache.asMap().forEach((k, v) -> {
            if (FileUtils.isPath(k)) {
                // check file exists
                // String filePath = this.getCache(k, String.class);
                if (! new File(k).exists()) {
                    durableCache.invalidate(k);
                }
            }
        });
    }

    /**
     * 暴露写文件的能力
     */
    public boolean writeFile(String filePath, String content) {
        try {
            FileUtils.createAndWriteFile(filePath, content);
        } catch (IOException e) {
            LogUtils.error(e);
            return false;
        }
        return true;
    }

    public boolean copyFile(URL resource, String targetPath) {
        try {
            FileUtils.copyFile(resource, targetPath);
        } catch (IOException e) {
            LogUtils.error(e);
            return false;
        }
        return true;
    }
}
