package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.python.PyClient;
import com.xhf.leetcode.plugin.debug.execute.python.PyContext;
import com.xhf.leetcode.plugin.debug.execute.python.PythonInstFactory;
import com.xhf.leetcode.plugin.debug.execute.python.PythonRBAInst;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugger extends AbstractDebugger {

    private final PythonDebugConfig config;
    private PythonDebugEnv env;

    private InstReader reader;

    private Output output;
    private PyContext context;
    /**
     * 启动python服务的Process
     */
    private Process exec;

    public PythonDebugger(Project project, PythonDebugConfig config) {
        super(project, new PyContext(), config, PythonInstFactory.getInstance());
        reader = config.getReader();
        output = config.getOutput();
        context = (PyContext) super.basicContext;
        this.config = config;
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
            startPythonService();
            initCtx();
            executePythonDebugRemotely();
        } catch (DebugError e) {
            LogUtils.error(e);
            ConsoleUtils.getInstance(project).showError(e.toString(), false, true);
        }
        DebugManager.getInstance(project).stopDebugger();

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
        while (DebugManager.getInstance(project).isDebug()) {
            ProcessResult pR = processDebugCommand();
            if (pR.isContinue) {
                continue;
            } else if (pR.isReturn) {
                return;
            }
            if (!pR.isSuccess) {
                LogUtils.simpleDebug("未知异常! debug 指令执行错误!");
                ConsoleUtils.getInstance(project).showError("未知异常! debug 指令执行错误!", false, true);
                continue;
            }
            if (Constants.PY_SERVER_DISCONNECT.equals(pR.r.getMoreInfo())) {
                LogUtils.simpleDebug("python服务断开连接, debug结束!");
                ConsoleUtils.getInstance(project).showInfo("python服务断开连接, debug结束!", false, true);
                break;
            }
        }
    }

    /**
     * 启动python服务
     */
    private void startPythonService() {
        String python = env.getPython();
        String cmd = String.format("\"%s\" \"%s\"", python, env.getMainPyPath());
        DebugUtils.simpleDebug("启动python服务: " + cmd, project);

        try {
            this.exec = Runtime.getRuntime().exec(cmd);
            DebugUtils.printProcess(exec, true, project);
        } catch (Exception e) {
            throw new DebugError(e.toString(), e);
        }

        // 五次检测连接
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            if (isPortAvailable("localhost", env.getPyPort())) {
                DebugUtils.simpleDebug("python服务连接成功", project, false);
                return;
            }
        }
        throw new DebugError("python服务连接失败!");
    }

    /**
     * 检查指定端口是否已经启动
     *
     * @param host 主机地址，例如 "localhost" 或 "127.0.0.1"
     * @param port 端口号
     * @return 如果端口已经启动，返回 true；否则返回 false
     */
    public static boolean isPortAvailable(String host, int port) {
        try (Socket ignored = new Socket(host, port)) {
            // 如果能够成功建立连接，说明端口已经启动
            return true;
        } catch (IOException e) {
            // 如果抛出异常，说明端口未启动或不可访问
            return false;
        }
    }

    @Override
    public void stop() {
        // 已经停止了, 无需再次停止
        if (!DebugManager.getInstance(project).isDebug()) {
            return;
        }
        DebugUtils.simpleDebug("PythonDebugger即将停止!", project);
        env.stopDebug();
        // 发送终止请求(所谓的终止python, 就是提前让python跑完所有内容, 自动结束)
        new PythonRBAInst().execute(Instruction.success(config.getReadType(), Operation.RBA, ""), this.context);
        new PythonRBAInst().execute(Instruction.success(config.getReadType(), Operation.R, ""), this.context);
        // 3次循环, 检测python服务是否已经关闭
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            if (!isPortAvailable("localhost", env.getPyPort())) {
                DebugUtils.simpleDebug("python服务关闭成功, PythonDebugger停止", project);
                return;
            }
        }
        exec.destroy();
        if (exec.isAlive()) {
            exec.destroyForcibly();
        }
        DebugUtils.simpleDebug("python服务强制关闭! PythonDebugger停止", project, false);
    }

    @Override
    public DebugEnv getEnv() {
        return this.env;
    }
}
