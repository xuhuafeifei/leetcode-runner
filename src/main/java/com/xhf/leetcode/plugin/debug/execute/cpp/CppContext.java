package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.debug.env.CppDebugEnv;
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
public class CppContext extends AbstractExecuteContext {
    private CppDebugEnv env;
    private CppClient cppClient;
    private ReadType readType;

    public CppContext(Project project) {
        super(project);
    }

    @Override
    public void removeWatchPoolListener(WatchPoolRemoveEvent event) {
        String data = event.getData();
        DebugUtils.simpleDebug("data = " + data, project);
        if (StringUtils.isBlank(data)) {
            return;
        }
        String[] split = data.split("=");
        if (split.length < 2) {
            DebugUtils.simpleDebug("cpp 无需删除watch pool", project);
        }
        split[0] = split[0].trim();
        // match
        List<String> arr = watchPool.stream().filter(e -> !e.startsWith(split[0])).collect(Collectors.toList());
        watchPool.clear();
        watchPool.addAll(arr);
    }

    public CppDebugEnv getEnv() {
        return env;
    }

    public void setEnv(CppDebugEnv env) {
        this.env = env;
    }

    public CppClient getCppClient() {
        return cppClient;
    }

    public void setCppClient(CppClient cppClient) {
        this.cppClient = cppClient;
    }

    public void setReadType(ReadType readType) {
        this.readType = readType;
    }

    public ReadType getReadType() {
        return this.readType;
    }
}
