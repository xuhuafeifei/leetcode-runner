package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.env.CppDebugEnv;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.execute.cpp.CppContext;
import com.xhf.leetcode.plugin.debug.execute.cpp.CppInstFactory;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CPPDebugger extends AbstractDebugger {

    private final CppContext context;
    private final CppDebugConfig config;
    private CppDebugEnv env;

    public CPPDebugger(Project project, CppDebugConfig config) {
        super(project, new CppContext(project), config, CppInstFactory.getInstance());
        this.context = (CppContext) super.basicContext;
        this.config = config;
        // this.outputHelper = new OutputHelper(project);
    }

    @Override
    public void start() {
        this.env = new CppDebugEnv(project);
        try {
            if (!env.prepare()) {
                env.stopDebug();
                return;
            }
        } catch (Exception e) {
            ConsoleUtils.getInstance(project).showError(e.toString(), false, true);
            LogUtils.error(e);
            return;
        }

        // 需要开启新线程, 否则指令读取操作会阻塞idea渲染UI的主线程
        new Thread(this::startDebug).start();
    }

    private void startDebug() {
    }

    @Override
    public void stop() {

    }

    @Override
    public DebugEnv getEnv() {
        return null;
    }
}
