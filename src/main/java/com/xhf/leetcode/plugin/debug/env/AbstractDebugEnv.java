package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.UnSafe;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.JTextArea;
import org.apache.commons.lang3.ArrayUtils;

/**
 * DebugEnv父类
 * 1. 在构造函数中初始化debug的中间文件存储路径——filePath, 允许子类通过initFilePath重写filePath路径
 * 2. DebugManager对外暴露isDebug接口, 使其拥有判断debug是否启动的能力
 * 3. 提供测试案例获取的能力, 也就是程序输入
 * 4. 提供stop, end接口
 * 5. 提供文件拷贝能力
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractDebugEnv implements DebugEnv {

    protected static boolean isDebug = false;
    protected final Project project;
    /**
     * 核心存储路径. 所有debug相关文件都存储在filePath指定目录下
     */
    protected String filePath = "";
    /**
     * Solution核心方法名
     */
    protected String methodName;

    public AbstractDebugEnv(Project project) {
        this.project = project;
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").build();
        initFilePath();
    }

    /**
     * 允许子类覆盖filePath路径
     */
    protected abstract void initFilePath();


    @Override
    public abstract boolean prepare() throws DebugError;

    protected boolean testcasePrepare() {
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByCurrentVFile(project);
        JTextArea jTextPane = new JTextArea(10, 40);
        jTextPane.setLineWrap(false);
        // 获取之前存储的debug testcase
        jTextPane.setText(lc.getDebugTestcase());

        DialogWrapper dialogWrapper = ViewUtils.getDialogWrapper(
            new JBScrollPane(jTextPane),
            BundleUtils.i18n("debug.leetcode.testcase.input"));

        int i = dialogWrapper.getExitCode();

        if (i != DialogWrapper.OK_EXIT_CODE) {
            return false;
        }

        String testcase = jTextPane.getText();
        lc.setDebugTestcase(testcase);
        // update
        ViewUtils.updateLeetcodeEditorByCurrentVFile(project, lc);
        return true;
    }

    @Override
    public boolean isDebug() {
        return isDebug;
    }

    @Override
    public void stopDebug() {
        isDebug = false; // 先置为false, 否则后续判断debug状态可能会出现问题. 在结束时可能会出现并发问题. DebugStopAction一个Thread, BQ消费一个Thread
        InstSource.clear();
        DebugUtils.removeHighlightLine(project);
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.env.stop"), project);
        LCEventBus.getInstance().post(new DebugEndEvent());
    }

    public void startDebug() {
        isDebug = true;
        InstSource.clear();
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.env.start"), project);

        LCEventBus.getInstance().post(new DebugStartEvent());
    }

    protected boolean copyFile() {
        return true;
    }

    protected boolean copyFileHelper(String resourcePath) {
        String[] split = resourcePath.split("/");
        String fileName = split[split.length - 1];
        return StoreService.getInstance(project).
            copyFile(getClass().getResource(resourcePath),
                new FileUtils.PathBuilder(filePath).append(fileName).build()
            );
    }

    /**
     * @param resourcePath 资源路径 [别在屁股后面加/]
     * @param exceptFileName 需要排除的文件名
     */
    protected boolean copyFileExcept(String resourcePath, String[] exceptFileName) {
        try {
            return forceTraverse(resourcePath, exceptFileName);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new DebugError(e);
        }
    }

    /**
     * 暴力遍历文件目录, 并copy文件
     *
     * @param resourcePath resource目录下的资源路径, 方法会拷贝目录下所有内容
     * @param exp 排除的文件名字
     * @return 是否成功
     */
    @UnSafe("这个方法没有经过各种场景的测试, 可能会存在风险. 目前对于/debug/python资源目录下的内容处理没有问题")
    private boolean forceTraverse(String resourcePath, String[] exp) throws IOException {
        // 判断路径是否是目录
        URL resourceURL = getClass().getResource(resourcePath);
        if (resourceURL == null) {
            return false;
        }
        // 从 URL 获取 JAR 文件路径
        String filePath = resourceURL.toString();
        // 判断是否是 JAR 文件
        if (filePath.startsWith("jar:file:")) {
            filePath = filePath.replace("jar:file:", "");
            String jarFilePath = filePath.substring(0, filePath.indexOf("!"));
            jarFilePath = URLDecoder.decode(jarFilePath, StandardCharsets.UTF_8);
            if (!FileUtils.fileExists(jarFilePath)) {
                throw new DebugError("jar file not found: " + jarFilePath);
            }

            JarFile jarFile = new JarFile(jarFilePath);

            // 遍历 JAR 文件中的条目
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entryName.startsWith("/")) {
                    entryName = "/" + entryName;
                }
                if (!resourcePath.startsWith("/")) {
                    resourcePath = "/" + resourcePath;
                }

                // 判断条目是否是目标目录下的文件或目录
                if (entryName.startsWith(resourcePath)) {
                    if (!entry.isDirectory()) {
                        // 文件
                        String[] split = entryName.split("/");
                        String fileName = split[split.length - 1];

                        // 判断是否需要排除文件
                        if (!ArrayUtils.contains(exp, fileName)) {
                            // 去除entryName的资源路径名称, 获取相对路径
                            if (!copyFileHelper(entry, entryName.replace(resourcePath, ""), jarFile)) {
                                // 复制文件
                                return false;
                            }
                        }
                    }
                }
            }

            jarFile.close();
        }
        return true;
    }

    /**
     * 复制文件的辅助方法
     *
     * @param entry JAR 文件中的条目
     * @param jarFile JAR 文件
     */
    private boolean copyFileHelper(JarEntry entry, String fileName, JarFile jarFile) throws IOException {
        InputStream inputStream = jarFile.getInputStream(entry);
        String targetPath = new FileUtils.PathBuilder(this.filePath).append(fileName).build();
        LogUtils.simpleDebug("targetPath: " + targetPath);
        boolean flag = FileUtils.copyFile(inputStream, targetPath);
        inputStream.close();
        return flag;
    }


    protected abstract boolean buildToolPrepare() throws DebugError;

    protected abstract boolean createMainFile() throws DebugError;

    protected abstract boolean createSolutionFile() throws DebugError;

}
