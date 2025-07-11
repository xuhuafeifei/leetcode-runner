package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.python.AbstractPythonInstExecutor;
import com.xhf.leetcode.plugin.debug.execute.python.PyClient;
import com.xhf.leetcode.plugin.debug.execute.python.PyContext;
import com.xhf.leetcode.plugin.debug.execute.python.PythonBInst;
import com.xhf.leetcode.plugin.debug.execute.python.PythonInstFactory;
import com.xhf.leetcode.plugin.debug.execute.python.PythonRBAInst;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.output.OutputHelper;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import java.util.List;
import java.util.Objects;

/**
 * Python Debugger 断点原理
 * <p>
 * 1. 启动python服务, 同时通过sys.settrace()方法, 执行断点逻辑.
 * 1.1. python服务和断点程序:
 * python服务和断点程序分别属于两个不同的线程, 他们只见通过阻塞队列通信.
 * <p>
 * 1.2. python服务与断点服务通信:
 * python服务接受客户端的指令, 处理并将指令存入A-BlockingQueue
 * 断点服务从A-BlockingQueue读取指令, 并执行指令, 获取数据后将数据存入B-BlockingQueue
 * python服务从B-BlockingQueue读取数据, 并将数据发送给客户端
 * <p>
 * 2. PythonDebugger与Python服务通信:
 * 2.1 PythonDebugger接受来自用户的指令, 指令可以来自于commandline, ui. 不论何种来源, python debugger
 * 都会处理成对应指令, 最终将指令通过HTTP请求的方式发送给python服务.
 * 而python服务内部会处理指令, 并通知断点线程执行相应操作, 最后通过HTTP Response返回处理结果
 * <p>
 * 2.2 PythonDebugger接受python服务返回的数据, 数据处理后通过Output模块进行可视化展示, 不论是UI展示亦或是Console展示
 * <p>
 * 3. PythonDebugger和JavaDebugger的区别:
 * <p>
 * 3.1. PythonDebugger的底层执行语言是python, JavaDebugger的底层执行语言是Java
 * <p>
 * 3.2. PythonDebugger采用HTTP请求的方式进行数据通信, 换句话说, PythonDebugger是主动获取数据. 由PythonDebugger通知python服务处理指令, 获取数据
 * 而JavaDebugger采用事件处理的方式处理数据, 由底层Java代码执行断点逻辑后, 通过event返回给JavaDebugger. 换句话说, JavaDebugger是被动获取数据.
 * JavaDebugger通过event获取数据, 然后进一步显示
 * <p>
 * 3.3. PythonDebugger, 底层断点执行逻辑无法通知PythonDebugger(因为HTTP是单向沟通)
 * JavaDebugger, 底层断点执行逻辑可以通知JavaDebugger, 并采用event封装数据
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugger extends AbstractDebugger {

    private final PythonDebugConfig config;
    private PythonDebugEnv env;

    private final InstReader reader;

    private final Output output;
    private final PyContext context;
    /**
     * 启动python服务的Process
     */
    private Process exec;
    private final OutputHelper outputHelper;

    public PythonDebugger(Project project, PythonDebugConfig config) {
        super(project, new PyContext(project), config, PythonInstFactory.getInstance());
        reader = config.getReader();
        output = config.getOutput();
        context = (PyContext) super.basicContext;
        this.config = config;
        this.outputHelper = new OutputHelper(project);
    }

    /**
     * 检查指定端口是否已经启动
     *
     * @param host 主机地址，例如 "localhost" 或 "127.0.0.1"
     * @param port 端口号
     * @return 如果端口已经启动，返回 true；否则返回 false
     */
    public static boolean isPortAvailable(String host, int port) {
        return DebugUtils.isPortAvailable(host, port);
    }

    @Override
    public void start() {
        this.env = new PythonDebugEnv(project, this.config);
        boolean flag = super.envPrepare(env);
        if (!flag) {
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
        this.context.setPyClient(new PyClient(project));
        this.context.setReadType(config.getReadType());
    }

    /**
     * 远程执行python debug功能
     */
    private void executePythonDebugRemotely() {
        // 监听stdout/stderr
        captureStd(OutputHelper.STD_OUT, 0, env.getStdOutDir(), outputHelper);
        captureStd(OutputHelper.STD_ERROR, 1, env.getStdErrDir(), outputHelper);
        // 初始化断点
        initBreakpoint();
        doRun();
    }

    private void doRun() {
        while (DebugManager.getInstance(project).isDebug()) {
            ProcessResult pR = processDebugCommand();
            if (pR.isContinue) {
                continue;
            } else if (pR.isReturn) {
                return;
            }
            if (!pR.isSuccess) {
                LogUtils.simpleDebug(BundleUtils.i18nHelper("未知异常! debug 指令执行错误!",
                    "unknown error! debug command execute error!"));
                ConsoleUtils.getInstance(project)
                    .showError(BundleUtils.i18n("action.leetcode.unknown.error"), false, true);
                continue;
            }
            if (Constants.PY_SERVER_DISCONNECT.equals(pR.r.getMoreInfo())) {
                LogUtils.simpleDebug(
                    BundleUtils.i18nHelper("python服务断开连接, debug结束!", "python server disconnect, debug end!"));
                ConsoleUtils.getInstance(project)
                    .showInfo(BundleUtils.i18n("debug.leetcode.debug.server.stop"), false, true);
                break;
            }
        }
    }

    private void initBreakpoint() {
        // ui读取模式下, 初始化断点
        if (AppSettings.getInstance().isUIReader()) {
            uiBreakpointInit();
        } else {
            commandBreakpointInit();
        }
    }

    private void commandBreakpointInit() {
        // PythonDebugger什么都不用做, 这部分逻辑python server端自动处理
    }

    private void uiBreakpointInit() {
        // 获取所有断点
        List<XBreakpoint<?>> allBreakpoint = DebugUtils.getAllBreakpoint(project);
        for (XBreakpoint<?> breakpoint : allBreakpoint) {
            XSourcePosition position = breakpoint.getSourcePosition();
            if (position == null) {
                continue;
            }
            VirtualFile file = Objects.requireNonNull(position).getFile();
            // 如果file和当前打开的vile一致, 设置断点信息
            if (file.equals(ViewUtils.getCurrentOpenVirtualFile(project))) {
                Instruction instruction = DebugUtils.buildBInst(position);
                // 设置断点
                new PythonBInst().execute(instruction, context);
            }
        }
        // 发送断点初始化done指令
        // 需要注意的是, 断点初始化done指令, 并没有集成到Operation 枚举中, 因为该指令只是单独服务于python断点服务
        // 不具备通用性质, 因此并不进行集成
        ExecuteResult res = new AbstractPythonInstExecutor() {
            @Override
            protected ExecuteResult doExecute(Instruction inst, PyContext pCtx) {
                PyClient pyClient = pCtx.getPyClient();
                PyClient.PyResponse pyResponse = pyClient.postRequest(Constants.OPT_PYTHON_INIT_BREAKPOINT_DONE, "");
                if (pyResponse == null) {
                    // 连接断开
                    return null;
                }
                return pyResponse.getData();
            }
        }.execute(null, context);
        // 连接断开, 终止debug
        if (res == null) {
            DebugManager.getInstance(project).stopDebugger();
            return;
        }

        /*
          这里区别于JavaDebugger, PythonDebugger需要主动获取断点信息, 而不是由底层断点逻辑通知
          因为PythonDebugger底层采用HTTP方式单向沟通python服务, 因此python服务如果遇到断点, PythonDebugger
          是无法感知到的. 因此, 如果想要实现运行到断点就高亮显示的功能, 只能由PythonDebugger主动发出请求
         */
        // 遇到断点后, 高亮显示
        InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, null));
        // 遇到断点后, 打印变量
        InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, null));
    }

    /**
     * 启动python服务
     */
    private void startPythonService() {
        String python = env.getPython();
        DebugUtils.simpleDebug(
            BundleUtils.i18n("debug.leetcode.server.start.cmd") + ": " + python + " " + env.getMainPyPath(), project);

        try {
            // fix #40
            this.exec = DebugUtils.buildProcess(python, env.getMainPyPath());
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
            if (DebugUtils.isPortAvailable2("localhost", env.getPyPort())) {
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.server.connect.succuess"), project, false);
                return;
            }
        }
        /*
        int i = this.exec.exitValue();
        // 如果正常退出, 表示断点服务跑完了
        if (i == 0) {
            DebugManager.getInstance(project).stopDebugger();
            return;
        }
         */
        throw new DebugError(BundleUtils.i18n("debug.leetcode.server.connect.failed"));
    }

    @Override
    public void stop() {
        // 已经停止了, 无需再次停止
        if (!DebugManager.getInstance(project).isDebug()) {
            return;
        }
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stopsoon"), project);
        env.stopDebug();
        // 打断监控线程
        for (Thread thread : threads) {
            if (thread != null) {
                thread.interrupt();
            }
        }
        // 如果没有启动, 直接返回
        if (!isPortAvailable("localhost", env.getPyPort())) {
            DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop"), project);
            return;
        }
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
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop"), project);
                return;
            }
        }
        exec.destroy();
        if (exec.isAlive()) {
            exec.destroyForcibly();
        }
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop.force"), project);
    }

    @Override
    public DebugEnv getEnv() {
        return this.env;
    }
}
