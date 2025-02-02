package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.debug.env.CppDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.AbstractExecuteContext;
import com.xhf.leetcode.plugin.debug.reader.ReadType;

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
