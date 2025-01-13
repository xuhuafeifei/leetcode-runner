package com.xhf.leetcode.plugin.debug;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.debugger.*;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@Service(Service.Level.PROJECT) // 注册idea, 保证所有debugger强制关闭
public final class DebugManager implements Disposable {
    private static volatile DebugManager instance;
    private final Project project;

    public DebugManager(Project project) {
        this.project = project;
        LCEventBus.getInstance().register(this);
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

    private Debugger currentDebugger;

    /*
     * 创建并存储全新的debugger. 同时关闭已有debugger
     */
    public Debugger createDebugger(Class<? extends Debugger> clazz) {
        closeAllDebugger();
        if (clazz == JavaDebugger.class) {
            JavaDebugger javaDebugger = buildJavaDebugger();
            debuggers.put(clazz, javaDebugger);
            currentDebugger = javaDebugger;
        } else if (clazz == PythonDebugger.class) {
            PythonDebugger pythonDebugger = buildPythonDebugger();
            debuggers.put(clazz, pythonDebugger);
            currentDebugger = pythonDebugger;
        }
        return currentDebugger;
    }

    /**
     * 关闭所有debugger
     */
    private void closeAllDebugger() {
        for (Debugger debugger : debuggers.values()) {
            debugger.stop();
        }
        currentDebugger = null;
    }

    public <T extends Debugger> T getDebugger(Class<T> clazz) {
        return clazz.cast(debuggers.get(clazz));
    }

    public Debugger getCurrentDebugger() {
        return currentDebugger;
    }

    public void stopDebugger() {
        if (currentDebugger != null) {
            currentDebugger.stop();
            currentDebugger = null;
        }
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

    public boolean isDebug() {
        if (currentDebugger == null) return false;
        if (currentDebugger.getEnv() == null) return false;
        return currentDebugger.getEnv().isDebug();
    }


    @Override
    public void dispose() {
        // 包装强制关闭所有的debugger
        LogUtils.simpleDebug("强制关闭所有debugger...");
        closeAllDebugger();
    }
}
