package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.env.CppDebugEnv;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.execute.cpp.CppContext;
import com.xhf.leetcode.plugin.debug.execute.cpp.CppInstFactory;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
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
    private Process exec;

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
        env.startDebug();
        try {
            startCppService();
            initCtx();
            executeCppDebugRemotely();
        } catch (DebugError e) {
            LogUtils.error(e);
            ConsoleUtils.getInstance(project).showError(e.toString(), false, true);
        }
        DebugManager.getInstance(project).stopDebugger();

    }

    private void executeCppDebugRemotely() {
        
    }

    private void initCtx() {

    }

    private void startCppService() {
        String serverMainExePath = env.getServerMainExePath();
        DebugUtils.simpleDebug("启动cpp服务: " + serverMainExePath, project);

        try {
            String cmd = env.getServerMainExePath();
            this.exec = Runtime.getRuntime().exec(cmd);
            DebugUtils.printProcess(exec, true, project);
        } catch (Exception e) {
            throw new DebugError(e.toString(), e);
        }

        // 五次检测连接(3s还连接不上, 挂了)
        for (int i = 0; i < 6; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            if (DebugUtils.isPortAvailable("localhost", env.getPort())) {
                DebugUtils.simpleDebug("cpp服务连接成功", project, false);
                return;
            }
        }
        int i = this.exec.exitValue();
        // 如果正常退出, 表示断点服务跑完了
        if (i == 0) {
            DebugManager.getInstance(project).stopDebugger();
            return;
        }
        throw new DebugError("cpp服务连接失败! 错误信息可通过Console查看");
    }

    @Override
    public void stop() {

    }

    @Override
    public DebugEnv getEnv() {
        return null;
    }
}
