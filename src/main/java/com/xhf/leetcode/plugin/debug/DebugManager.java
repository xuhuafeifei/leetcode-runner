package com.xhf.leetcode.plugin.debug;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.debugger.CPPDebugger;
import com.xhf.leetcode.plugin.debug.debugger.CppDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.Debugger;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugger;
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
    // 容器
    private final Map<Class<? extends Debugger>, Debugger> debuggers = new HashMap<>();
    private Debugger currentDebugger;

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
        } else if (clazz == CPPDebugger.class) {
            CPPDebugger cppDebugger = buildCppDebugger();
            debuggers.put(clazz, cppDebugger);
            currentDebugger = cppDebugger;
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
            try {
                currentDebugger.stop();
            } catch (Exception e) {
                LogUtils.warn("debugger关闭出现异常! cause = " + e.getMessage());
            }
            currentDebugger = null;
        }
    }

    private CPPDebugger buildCppDebugger() {
        CppDebugConfig config = null;
        try {
            config = new CppDebugConfig.Builder(project).autoBuild().build();
        } catch (Exception ex) {
            LogUtils.error(ex);
            throw new DebugError("Java环境配置创建异常!" + ex, ex);
        }
        return new CPPDebugger(project, config);
    }

    private PythonDebugger buildPythonDebugger() {
        PythonDebugConfig config = null;
        try {
            config = new PythonDebugConfig.Builder(project).autoBuild().build();
        } catch (Exception ex) {
            LogUtils.error(ex);
            throw new DebugError("Java环境配置创建异常!" + ex, ex);
        }
        return new PythonDebugger(project, config);
    }

    private JavaDebugger buildJavaDebugger() {
        JavaDebugConfig config = null;
        try {
            config = new JavaDebugConfig.Builder(project).autoBuild().build();
        } catch (Exception ex) {
            LogUtils.error(ex);
            throw new DebugError("Java环境配置创建异常!" + ex, ex);
        }
        return new JavaDebugger(project, config);
    }

    public Debugger getAnyDebugger() {
        return debuggers.values().iterator().next();
    }

    public boolean isDebug() {
        if (currentDebugger == null) {
            return false;
        }
        if (currentDebugger.getEnv() == null) {
            return false;
        }
        return currentDebugger.getEnv().isDebug();
    }


    @Override
    public void dispose() {
        // 包装强制关闭所有的debugger
        LogUtils.simpleDebug("强制关闭所有debugger...");
        closeAllDebugger();
    }
}
