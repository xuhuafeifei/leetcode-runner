package com.xhf.leetcode.plugin.debug.execute;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import org.apache.commons.lang3.StringUtils;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractExecuteContext implements ExecuteContext {
    /**
     * 监控池, 存储被watch的表达式
     */
    protected final Deque<String> watchPool = new LinkedList<>();

    protected Project project;

    public AbstractExecuteContext(Project project) {
        this.project = project;
    }

    @Override
    public void debugStartListener(DebugStartEvent event) {
        String cache = StoreService.getInstance(project).getCache(StoreService.WATCH_POOL_KEY, String.class);
        if (cache != null) {
            String[] split = cache.split("\n");
            for (String s : split) {
                if (StringUtils.isNotBlank(s)) {
                    watchPool.push(s);
                }
            }
        }
    }

    @Override
    public void debugStopListener(DebugEndEvent event) {
        // 缓存监听内容
        StringBuilder a = new StringBuilder();
        for (String s : watchPool) {
            a.append(s).append("\n");
        }
        StoreService.getInstance(project).addCache(StoreService.WATCH_POOL_KEY, a.toString());
        /*
          在v3.4.0版本中, 在ExecuteContext中引入LCEventBus, 会将其注入总线中心.
          这会导致stop结束, ExecuteContext已经不会被使用, 但并不会被gc. 因此需要手动
          将其从总线中心移除, 避免内存泄漏. 此外, 移除操作必须在最后执行
          否则ExecuteContext无法接收到事件, 进而执行额外逻辑
         */
        LCEventBus.getInstance().remove(this);
        DebugUtils.simpleDebug("移除ExecuteContext...", project);
    }

    @Override
    public Project getProject() {
        return project;
    }

    public void addToWatchPool(String s) {
        watchPool.push(s);
    }

    @Override
    public String[] getWatchPool() {
        String[] ans = new String[watchPool.size()];
        int i = 0;
        for (String s : watchPool) {
            ans[i] = s;
            i += 1;
        }
        return ans;
    }
}
