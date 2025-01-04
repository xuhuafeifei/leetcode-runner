package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.execute.java.JavaBInst;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * java debug event handler
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JEventHandler implements Runnable {
    private final Context context;
    private final Project project;
    private final JavaDebugEnv env;

    public JEventHandler(Context ctx) {
        this.context = ctx;
        this.project = context.getProject();
        this.env = ctx.getEnv();
        new Thread(this, "JEventHandler").start();
    }

    @Override
    public void run() {
        EventQueue eventQueue = context.getVm().eventQueue();
        while (true) {
            try {
                // 同理保留: 纪念为排查invokeMethod带来bug做出的杰出贡献
                // LogUtils.simpleDebug("准备阻塞..."); // 不要删除!!!
                EventSet set = eventQueue.remove();
                context.setEventSet(set);
                // LogUtils.simpleDebug("获取数据, 不再阻塞...."); // 不要删除!!!

                Iterator<Event> it = set.iterator();
                // copy JDB源码
                boolean resumeStoppedApp = false;

                while (it.hasNext()) {
                    // 只要有一个事件不resume, 就必须resume
                    resumeStoppedApp |= !handleEvent(it.next());
                }

                if (resumeStoppedApp) {
                    set.resume();
                }
            } catch (VMDisconnectedException ignored) {
                LogUtils.simpleDebug("debug stop...");
                return;
            } catch (InterruptedException | AbsentInformationException e) {
                LogUtils.error(e);
            }
        }
    }

    /**
     * 处理event, 同时返回是否resume
     *
     * @param event
     * @return false: 需要resume; true: 不需要resume, 需要阻塞
     * @throws AbsentInformationException
     */
    private boolean handleEvent(Event event) throws AbsentInformationException {
        /*
         debug: 该日志信息在处理invokeMethod导致的阻塞等诸多bug
         起到关键性作用, 为了纪念下方代码对@author feigebuge
         排查bug做出的杰出贡献, 保留注释forever...
         @date 2025/1/4 17:25
         */
        // LogUtils.simpleDebug(event.toString()); // 不要删除!!!

        // 设置context基本信息. 如果前台触发invokeMethod, 则不update location/thread信息
        if (event instanceof LocatableEvent && ! context.isInvokeMethodStart()) {
            setContextBasicInfo((LocatableEvent) event);
        }

        if (event instanceof ClassPrepareEvent) {
            return handleClassPrepareEvent((ClassPrepareEvent) event);
        } else if (event instanceof BreakpointEvent) {
            return handleBreakpointEvent((BreakpointEvent) event); // 需要停止
        } else if (event instanceof StepEvent) {
            return handleStepEvent((StepEvent) event); // 需要停止
        } else if (event instanceof VMStartEvent) {
            return false;
        } else if (event instanceof VMDisconnectEvent) {
            return handleVMDisconnectEvent((VMDisconnectEvent) event);
        } else {
            return true;
        }
    }



    private boolean handleVMDisconnectEvent(VMDisconnectEvent event) {
        DebugManager.getInstance(project).stopDebugger();
        return true;
    }

    private boolean handleStepEvent(StepEvent event) {
        /*
         此处需要区分是否因该update location/thread信息. 如果前台正在执行doExp方法, 并且触发invokeMethod
         那么不能修改context中的location, thread信息.
         因为invokeMethod会导致stack frame状态变化, 进而变化后的location, thread等数据
         此时如果执行非运行TargetVM类指令, 如W, P(不进行表达式计算的P指令), 那么会导致得不到预料输出
         比如:
         现在代码执行到 line 10:
         int a = 10
         此时用户输入P demo.test(). 那么JavaPInst在解析并执行表达式时, 底层会调用
         invokeMethod. 而该方法会改变TargetVM的状态, 最终导致的stack frame的变化
         此时在执行W, P指令, 得到的时test()方法内部的数据, 而不是int a = 10;这行
         代码所在方法的数据.
         因此需要做处location, thread数据的备份
         */
        if (! context.isInvokeMethodStart()) {
            context.setLocation(event.location());
            context.setThread(event.thread());
        }

        Thread.yield();  // fetch output

        // 如果doExp开启invokeMethod, 需要resume
        // 否则invokeMethod方法可能会被阻塞
        if (context.isInvokeMethodStart()) {
            LogUtils.simpleDebug("step event检测到doExp触发invokeMethod, resume TargetVM");
            return false;
        }
        LogUtils.simpleDebug("doExp并未触发invokeMethod, stop TargetVM");
        // 后台准备完毕, 前台可以执行指令
        context.done();
        return true;
    }

    private void commandBreakpointInit(ClassPrepareEvent classPrepareEvent) {
        // 获取solutionClass
        ClassType solutionClass = (ClassType) classPrepareEvent.referenceType();
        // 获取solution类的核心方法
        String methodName = env.getMethodName();
        Method mainMethod = solutionClass.methodsByName(methodName).get(0);
        Location location = mainMethod.location();

        context.setLocation(location);
        context.setSolutionLocation(location);

        // 设置断点
        new JavaBInst().execute(Instruction.success(ReadType.UI_IN, Operation.B, String.valueOf(location.lineNumber())), context);

        DebugUtils.simpleDebug("break point set at Solution." + mainMethod + " line " + location.lineNumber(), project);
    }

    private void uiBreakpointInit(ClassPrepareEvent classPrepareEvent) {
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
                new JavaBInst().execute(instruction, context);
            }
        }
    }

    private boolean handleBreakpointEvent(BreakpointEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());

        context.setBreakpointEvent(event);
        context.setThread(event.thread());
        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_OVER);

        Location location = event.location();
        String className = location.declaringType().name(); // 类名
        String methodName = location.method().name(); // 方法名
        int lineNumber = location.lineNumber(); // 行号

        String res = className + "." + methodName + ":" + lineNumber;

        DebugUtils.simpleDebug("Hit breakpoint at: " + res, project);

        context.setBreakpointEvent(event);

        // 设置单步请求
        setContextBasicInfo(event); // 这是初始化方法, 不能删除...
        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_INTO);

        if (AppSettings.getInstance().isUIOutput()) {
            // 输入UI打印指令. UI总是会在遇到断点时打印局部变量. 因此输入P指令
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, ""));
            // 高亮指令
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, ""));
        }

        // 后台准备完毕, 前台可以执行指令
        context.done();
        return true;
    }

    private boolean handleClassPrepareEvent(ClassPrepareEvent event) {
        String className = event.referenceType().name();
        if (className.equals("Solution")) {
            Location location = context.setSolutionLocation(event);
            context.setLocation(location);
            context.setThread(event.thread());

            // ui读取模式下, 初始化断点
            if (AppSettings.getInstance().isUIReader()) {
                uiBreakpointInit(event);
            }else {
                commandBreakpointInit(event);
            }
        }
        return false;
    }

    /**
     * 设置基础信息, 比如location和thread
     *
     * @param event
     */
    private void setContextBasicInfo(LocatableEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());
    }
}