package com.xhf.leetcode.plugin.debug.execute.java;

import com.intellij.openapi.project.Project;
import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.*;


/**
 * 指令执行上下文
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Context implements ExecuteContext {
    private BreakpointEvent breakpointEvent;
    private EventRequestManager erm;
    private EventSet eventSet;
    private ClassType currentClass;
    private VirtualMachine vm;
    private JavaDebugEnv env;
    /**
     * 表示当前代码执行的location
     */
    private Location location;
    private List<BreakpointRequest> breakpointRequests;
    private ThreadReference thread;
    private Project project;
    /**
     * 专门表示Solution类的location
     */
    private Location solutionLocation;
    /**
     * 单步请求管理器. 统一管理所有的单步请求
     */
    private final StepRequestManager stepRequestManager = new StepRequestManager();
    private Iterator<Event> itr;
    /**
     * 监控池, 存储被watch的表达式
     */
    private final Deque<String> watchPool = new LinkedList<>();

    /**
     * 项目运行之初, 需要等待
     */
    private volatile boolean waitFor = true;
    private Output output;

    public ClassType getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(ClassType currentClass) {
        this.currentClass = currentClass;
    }

    public BreakpointEvent getBreakpointEvent() {
        return breakpointEvent;
    }

    public void setBreakpointEvent(BreakpointEvent breakpointEvent) {
        this.breakpointEvent = breakpointEvent;
    }

    public EventRequestManager getErm() {
        return erm;
    }

    public void setErm(EventRequestManager erm) {
        this.erm = erm;
    }

    public EventSet getEventSet() {
		return eventSet;
	}

    public void setEventSet(EventSet eventSet) {
		this.eventSet = eventSet;
	}

    public void setStepRequest(int size, int depth) {
        stepRequestManager.setStepRequest(size, depth);
    }

    /**
     * 这个名字不好, 取错了, 但考虑该方法已经大面积使用, 就保留了
     */
    public void removeStepRequest() {
        stepRequestManager.disable();
    }

    public void disableStepRequest() {
        stepRequestManager.disable();
    }

    public void enableStepRequest() {
        stepRequestManager.enable();
    }

    public void setVm(VirtualMachine vm) {
        this.vm = vm;
    }

    public VirtualMachine getVm() {
		return vm;
	}

    public void setEnv(JavaDebugEnv env) {
        this.env = env;
    }

    public JavaDebugEnv getEnv() {
		return env;
	}

    public void setLocation(Location location) {
        this.location = location;
    }

    public synchronized Location getLocation() {
		return location;
	}

    public void setBreakpointRequestList(List<BreakpointRequest> breakpointRequests) {
        this.breakpointRequests = breakpointRequests;
    }

    public List<BreakpointRequest> getBreakpointRequests() {
		return breakpointRequests;
	}

    public void addBreakpointRequest(BreakpointRequest breakpointRequest) {
        this.breakpointRequests.add(breakpointRequest);
    }

    public void setThread(ThreadReference thread) {
        this.thread = thread;
    }

    public synchronized ThreadReference getThread() {
		return thread;
	}

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return this.project;
    }

    @Override
    public Deque<String> getWatchPool() {
        return watchPool;
    }

    public Location setSolutionLocation(ClassPrepareEvent event) {
        String className = event.referenceType().name();
        if (!className.equals("Solution")) {
            throw new DebugError("Context setSolutionLocation使用错误. 该方法只允许当className为Solution时使用!");
        }
        // 获取solutionClass
        ClassType solutionClass = (ClassType) event.referenceType();
        // 获取solution类的核心方法
        String methodName = env.getMethodName();
        Method mainMethod = solutionClass.methodsByName(methodName).get(0);
        // 获取location
        Location location = mainMethod.location();
        setSolutionLocation(location);
        return location;
    }

    public void setSolutionLocation(Location location) {
        this.solutionLocation = location;
    }

    public synchronized Location getSolutionLocation() {
        return this.solutionLocation;
    }

    public void setItrEvent(Iterator<Event> itr) {
        this.itr = itr;
    }

    public Iterator<Event> getItrEvent() {
        return this.itr;
    }

    public void consumeAllEvent() {
        while (this.itr != null && this.itr.hasNext()) {
            itr.next();
        }
    }

    public void resume() {
        if (vm == null) {
            throw new DebugError("Context resume使用错误. 请在通过Context恢复Target VM运行前, 先设置vm对象 !");
        }
        vm.resume();
    }

    public void addOutput(Output output) {
        this.output = output;
    }

    public Output getOutput() {
        return this.output;
    }

    /**
     * step request管理器, 统一管理step request. 防止
     * 项目发出多个同一个step request, 导致运行异常
     */
    private class StepRequestManager {
        private StepRequest stepRequest;
        void setStepRequest(int size, int depth) {
            if (stepRequest != null) {
                this.stepRequest.disable();
            }
            if (erm == null || thread == null) {
                throw new DebugError("Context setStepRequest使用错误. 请在使用context对StepRequest进行统一管理前, 先设置erm和thread对象 !");
            }
            this.stepRequest = erm.createStepRequest(thread, size, depth);
            this.stepRequest.enable();
        }
        void disable() {
            if (stepRequest != null) {
                stepRequest.disable();
            }
        }

        public void enable() {
            if (stepRequest != null) {
                stepRequest.enable();
            }
        }
    }

    /**
     * 锁
     */
    private final Object lock = new Object();
    /**
     * 存储调用waitFor的线程
     */
    @Deprecated // 打断线程的方式已被废弃. 打断线程存在破坏正在阻塞读取数据线程的风险. 详细查看{@link com.xhf.leetcode.plugin.debug.reader.InstSource}
    private final Set<Thread> threadSet = new HashSet<>();

    /**
     * 自选等待JEventHandler通知, 如果waitFor为False或者被打断/唤醒, 则表明JEventHandler完成处理
     * JavaDebugger可以继续执行
     */
    public void waitForJEventHandler(String name) {
        // 总是先让出cpu
        Thread.yield();
        // 无需等待
        if (! waitFor) {
            LogUtils.simpleDebug(name + "无需等待JEventHandler...");
            return;
        }
        synchronized (lock) {
            LogUtils.simpleDebug(name + "等待JEventHandler...");
            threadSet.add(Thread.currentThread());
            try {
                // 自旋优化
                for (int i = 0; i < 8 && waitFor; ++i) {
                    Thread.sleep(100);
                }
                LogUtils.simpleDebug("睡眠...");
                lock.wait();
                LogUtils.simpleDebug("JEventHandler释放锁, " + name + "执行指令...");
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * 调用此方法, JavaDebugger将会等待JEventHandler处理TargetVM
     * 直到JEventHandler调用{@link #JEventHandlerDone()}方法, JavaDebugger可以继续执行
     * <p>
     * 该方法服务于JavaDebugger. 其他类不能调用此方法
     */
    public void waitFor() {
        synchronized (lock) {
            this.waitFor = true;
        }
    }

    /**
     * 调用此方法, 表示JEventHandler已经完成运行, JavaDebugger无需等待
     * <p>
     * 该方法服务于JEventHandler. 其他类不能调用此方法
     */
    public void JEventHandlerDone() {
        this.waitFor = false;
        // 不再采用线程打断的方式唤醒
        // 因为此种打断方式可能会中断读取阻塞队列的线程
        /*
        for (Thread t : threadSet) {
            t.interrupt();
        }
         */
        synchronized (lock) {
            this.lock.notifyAll();
        }
    }

    /**
     * 存储前台执行invokeMethod的状态
     */
    private enum InvokeStatus {
        NOT_START, // 前台JavaDebugger没有执行invokeMethod, 属于初始状态
        INVOKE_START, // 前台JavaDebugger开始执行invokeMethod
        INVOKE_DONE // 前台JavaDebugger执行invokeMethod完成
    }

    /**
     * 存储invoke状态. 该字段记录JavaDebugger在处理指令过程中
     * 是否触发invokeMethod. invokeMethod底层涉及到很多复杂的操作,
     * 需要额外进行前后台的协调处理(JavaDebugger和JEventHandler之间的协调)
     */
    private InvokeStatus invokeStatus = InvokeStatus.NOT_START;

    /**
     * {@link com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl}准备进行invokeMethod, JEventHandler执行指令时
     * 会检测是否进行invokeMethod, 从而做出不同的处理逻辑
     * <p>
     * 该方法只服务于JavaEvaluatorImpl
     */
    public void invokeMethodStart() {
        invokeStatus = InvokeStatus.INVOKE_START;
    }

    /**
     * {@link com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl}完成invokeMethod
     * <p>
     * 该方法只服务于JavaEvaluatorImpl
     */
    public void invokeMethodDone() {
        invokeStatus = InvokeStatus.INVOKE_DONE;
        String s = DebugUtils.buildCurrentLineInfoByLocation(location);
        LogUtils.simpleDebug("invokeMethod done... " + s);
    }

    /**
     * 判断是否完成前台是否完成invokeMethod
     * 该方法只服务于JEventHandler
     */
    public boolean isInvokeMethodStart() {
        return invokeStatus == InvokeStatus.INVOKE_START;
    }
}