package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.java.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.execute.python.PyClient;
import com.xhf.leetcode.plugin.debug.execute.python.PyContext;
import com.xhf.leetcode.plugin.debug.execute.python.PythonInstFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.ConsoleOutput;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.reader.CommandLineReader;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugger extends AbstractDebugger {

    private PythonDebugEnv env;

    private InstReader reader;

    private Output output;
    private PyContext context;

    public PythonDebugger(Project project, PythonDebugConfig config) {
        super(project, new PyContext(), config, PythonInstFactory.getInstance());
        reader = new CommandLineReader(project);
        output = new ConsoleOutput(project);
        context = (PyContext) super.basicContext;
    }

    @Override
    public void start() {
        this.env = new PythonDebugEnv(project);
        try {
            if (!env.prepare()) {
                env.stopDebug();
                DebugUtils.simpleDebug("环境准备失败!", project, true);
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
//            startPythonService();
        } catch (DebugError e) {
            DebugUtils.simpleDebug("Debug Failed! " + e, project, true);
            return;
        }
        initCtx();
        executePythonDebugRemotely();
        env.stopDebug();
    }

    private void initCtx() {
        this.context.setEnv(env);
        this.context.setProject(project);
        this.context.setPyClient(new PyClient(project));
    }

    /**
     * 远程执行python debug功能
     */
    private void executePythonDebugRemotely() {
        while (true) {
            ProcessResult pR = processDebugCommand();
            if (! pR.isSuccess) {
                if (pR.isContinue) {
                    continue;
                }
                if (pR.isReturn) {
                    return;
                }
            }
        }
    }

    /**
     * 启动python服务
     */
    private void startPythonService() {
        String python = env.getPython();
        String cmd = String.format("\"%s\" \"%s\"", python, env.getMainPyPath());

        try {
            Process exec = Runtime.getRuntime().exec(cmd);
            DebugUtils.printProcess(exec, false, project);
        } catch (Exception e) {
            throw new DebugError(e.toString(), e);
        }

        // 三次检测连接
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            if (isPortAvailable("localhost", env.getPyPort())) {
                DebugUtils.simpleDebug("python服务启动成功", project, false);
                env.startDebug();
                return;
            }
        }
        throw new DebugError("python服务启动失败!");
    }

    /**
     * 检查指定端口是否已经启动
     *
     * @param host 主机地址，例如 "localhost" 或 "127.0.0.1"
     * @param port 端口号
     * @return 如果端口已经启动，返回 true；否则返回 false
     */
    public static boolean isPortAvailable(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            // 如果能够成功建立连接，说明端口已经启动
            return true;
        } catch (IOException e) {
            // 如果抛出异常，说明端口未启动或不可访问
            return false;
        }
    }

    @Override
    public void stop() {
        env.stopDebug();
        // 发送终止请求
//        dafjl
    }

    @Override
    public DebugEnv getEnv() {
        return this.env;
    }
}
