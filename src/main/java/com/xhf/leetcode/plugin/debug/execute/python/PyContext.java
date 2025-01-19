package com.xhf.leetcode.plugin.debug.execute.python;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.AbstractExecuteContext;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PyContext extends AbstractExecuteContext {
    private PythonDebugEnv env;
    private PyClient pyClient;
    private ReadType readType;

    public PyContext(Project project) {
        super(project);
        LCEventBus.getInstance().register(this);
    }

    public void setEnv(PythonDebugEnv env) {
        this.env = env;
    }

    public PythonDebugEnv getEnv() {
        return this.env;
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

    /**
     * this.b watch pool
     * this.b = xxxxx data
     * 这段定位逻辑存在问题, 如果用户输入的表达式存在大量空格, startWith匹配则会失效
     * @param event event
     */
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
