package com.xhf.leetcode.plugin.debug;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.debugger.*;
import com.xhf.leetcode.plugin.debug.env.AbstractDebugEnv;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugManager {
    private static volatile DebugManager instance;
    private final Project project;

    private DebugManager(Project project) {
        this.project = project;
    }
    // 单例
    public static DebugManager getInstance(Project project) {
        if (instance == null) {
            synchronized (DebugManager.class) {
                if (instance == null) {
                    instance = new DebugManager(project);
                }
            }
        }
        return instance;
    }

    // 容器
    private final Map<Class<? extends Debugger>, Debugger> debuggers = new HashMap<>();

    /*
     * 创建并存储全新的debugger
     */
    public Debugger createDebugger(Class<? extends Debugger> clazz) {
        if (clazz == JavaDebugger.class) {
            JavaDebugger javaDebugger = buildJavaDebugger();
            debuggers.put(clazz, javaDebugger);
            return javaDebugger;
        } else if (clazz == PythonDebugger.class) {
            PythonDebugger pythonDebugger = buildPythonDebugger();
            debuggers.put(clazz, pythonDebugger);
            return pythonDebugger;
        }
        return null;
    }

    public <T extends Debugger> T getDebugger(Class<T> clazz) {
        return clazz.cast(debuggers.get(clazz));
    }


    private PythonDebugger buildPythonDebugger() {
        PythonDebugConfig config = null;
        try {
            config = new PythonDebugConfig.Builder(project).autoBuild().build();
        } catch (Exception ex) {
            LogUtils.error(ex);
            throw new DebugError("Java环境配置创建异常!" + ex.toString(), ex);
        }
        return new PythonDebugger(project, config);
    }

    private JavaDebugger buildJavaDebugger() {
        JavaDebugConfig config = null;
        try {
            config = new JavaDebugConfig.Builder(project).autoBuild().build();
        } catch (Exception ex) {
            LogUtils.error(ex);
            throw new DebugError("Java环境配置创建异常!" + ex.toString(), ex);
        }
        return new JavaDebugger(project, config);
    }

    public Debugger getAnyDebugger() {
        return debuggers.values().iterator().next();
    }
}
