package com.xhf.leetcode.plugin.debug.execute.python;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PyContext implements ExecuteContext {
    private Project project;
    private PythonDebugEnv env;
    private PyClient pyClient;
    private ReadType readType;
    /**
     * 监控池, 存储被watch的表达式
     */
    private final Deque<String> watchPool = new LinkedList<>();

    public PyContext() {
        LCEventBus.getInstance().register(this);
    }

    public void setEnv(PythonDebugEnv env) {
        this.env = env;
    }

    public PythonDebugEnv getEnv() {
        return this.env;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public Project getProject() {
        return this.project;
    }

    @Override
    public Deque<String> getWatchPool() {
        return watchPool;
    }

    public void setPyClient(PyClient pyClient) {
        this.pyClient = pyClient;
    }

    public PyClient getPyClient() {
        return this.pyClient;
    }

    public void setReadType(ReadType readType) {
        this.readType = readType;
    }

    public ReadType getReadType() {
        return this.readType;
    }

    @Subscribe
    @Override
    public void removeWatchPoolListener(WatchPoolRemoveEvent event) {
        String data = event.getData();

        DebugUtils.simpleDebug("data = " + data, project);
        if (StringUtils.isBlank(data)) {
            return;
        }
        String[] split = data.split(":");
        if (split.length < 2) {
            DebugUtils.simpleDebug("python 无需删除watch pool", project);
        }
        split[0] = split[0].trim();
        // match
        List<String> arr = watchPool.stream().filter(e -> !e.startsWith(split[0])).collect(Collectors.toList());
        watchPool.clear();
        watchPool.addAll(arr);
    }
}
