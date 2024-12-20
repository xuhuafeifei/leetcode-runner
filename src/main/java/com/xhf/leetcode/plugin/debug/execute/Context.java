package com.xhf.leetcode.plugin.debug.execute;

import com.intellij.openapi.project.Project;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;

import java.util.List;

/**
 * 指令执行上下文
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Context {
    private BreakpointEvent breakpointEvent;
    private String output;
    private EventRequestManager erm;
    private EventSet eventSet;
    private StepRequest stepRequest;
    private ClassType currentClass;
    private StepEvent stepEvent;
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

    public String getOutput() {
        String res = output;
        output = null;
        return res;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public EventRequestManager getErm() {
        return erm;
    }

    public void setErm(EventRequestManager erm) {
        this.erm = erm;
    }

    public void addOutput(String res) {
        this.output = res;
    }

    public EventSet getEventSet() {
		return eventSet;
	}

    public void setEventSet(EventSet eventSet) {
		this.eventSet = eventSet;
	}

    public StepRequest getStepRequest() {
		return stepRequest;
	}

    public void setStepRequest(StepRequest stepRequest) {
		this.stepRequest = stepRequest;
	}

    public void setStepEvent(StepEvent stepEvent) {
        this.stepEvent = stepEvent;
    }

    public StepEvent getStepEvent() {
		return stepEvent;
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

    public Location getLocation() {
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

    public ThreadReference getThread() {
		return thread;
	}

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return this.project;
    }

    public void setSolutionLocation(Location location) {
        this.solutionLocation = location;
    }

    public Location getSolutionLocation() {
        return this.solutionLocation;
    }
}
