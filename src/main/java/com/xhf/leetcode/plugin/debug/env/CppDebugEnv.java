package com.xhf.leetcode.plugin.debug.env;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.CppCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.converter.CppTestcaseConvertor;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.setting.InnerHelpTooltip;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

import static javax.swing.JOptionPane.OK_OPTION;

/**
 * 启动cpp环境的debug
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppDebugEnv extends AbstractDebugEnv {
    /**
     * gdb
     */
    private String GDB = "";
    /**
     * g++
     */
    private String GPP = "";
    /**
     * MinGW的目录
     */
    private String MINGW_HOME = "";
    /**
     * solution cpp path
     */
    private String solutionCppPath;
    /**
     * 创建的代码和用户编写的代码的偏移量
     */
    private int offset;
    /**
     * 端口
     */
    private int port;
    /**
     * ServerMain的路径
     */
    private String serverMainPath;
    /**
     * solution.exe的路径
     */
    private String solutionExePath;
    /**
     * ServerMain.exe的路径
     */
    private String serverMainExePath;

    public CppDebugEnv(Project project) {
        super(project);
    }

    @Override
    protected void initFilePath() {
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").append("cpp").build();
    }

    @Override
    public boolean prepare() throws DebugError {
        return buildToolPrepare() && testcasePrepare() && createSolutionFile() && createMainFile() && copyFile() && closeExe() && buildFile();
    }

    /**
     * 强制关闭exe, 该方法不论是否执行成功, 都不会返回false
     * @return boolean
     */
    private boolean closeExe() {
        this.solutionExePath = new FileUtils.PathBuilder(filePath).append("solution.exe").build();
        this.serverMainExePath = new FileUtils.PathBuilder(filePath).append("ServerMain.exe").build();

        try {
            FileUtils.removeFile(this.solutionExePath);
            LogUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.exe.clear.success") + ": " + this.solutionExePath);
        } catch (Exception e) {
            DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.exe.clear.failed") + ": " + this.solutionExePath + " " + BundleUtils.i18n("action.leetcode.plugin.cause") +  " = " + e.getMessage(), project, ConsoleViewContentType.ERROR_OUTPUT);
        }
        try {
            FileUtils.removeFile(this.serverMainExePath);
            LogUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.main.clear.success") + ": " + this.solutionExePath);
        } catch (Exception e) {
            DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.main.clear.failed") + ": " + this.serverMainExePath + " cause = " + e.getMessage(), project, ConsoleViewContentType.ERROR_OUTPUT);
        }
        return true;
    }

    @Override
    protected boolean copyFile() {
        // return copyFileHelper("/debug/cpp/leetcode.h");
        return
                copyFileExcept("/debug/cpp",
                        new String[] {
                                "test.cmd",
                                "ListNodeConvertor.template",
                                "TreeNodeConvertor.template",
                                "ServerMain.template",
                                "Main.template",
                                "Main.cpp"
                        }
                );
    }

    @Override
    protected boolean buildToolPrepare() throws DebugError {
        boolean flag = StoreService.getInstance(project).contains("MINGW_HOME");

        TextFieldWithBrowseButton myFileBrowserBtn = new TextFieldWithBrowseButton();
        myFileBrowserBtn.addBrowseFolderListener(
                new TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                ) {
                });

        // 携带帮助文档的按钮
        String HELP_CONTENT = BundleUtils.i18n("debug.leetcode.main.mingw.help");
        JPanel targetComponent;
        targetComponent = InnerHelpTooltip.BoxLayout().add(myFileBrowserBtn).addHelp(HELP_CONTENT).getTargetComponent();

        String javaPath;
        if (flag) {
            javaPath = StoreService.getInstance(project).getCache("MINGW_HOME", String.class);
            myFileBrowserBtn.setText(javaPath);
        }

        int i = JOptionPane.showOptionDialog(
                null,
                targetComponent,
                BundleUtils.i18nHelper("选择MinGW目录", "choose MinGW directory"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{BundleUtils.i18n("action.leetcode.plugin.ok"), BundleUtils.i18n("action.leetcode.plugin.cancel"), BundleUtils.i18n("debug.leetcode.main.mingw.no")},
                BundleUtils.i18n("action.leetcode.plugin.ok")
        );
        if (i == 2) {
            // 给出下载链接
            JOptionPane.showOptionDialog(
                    null,
                    FormBuilder.createFormBuilder()
                            .addLabeledComponent(new JBLabel(BundleUtils.i18n("debug.leetcode.github.link")), new JBTextField("https://github.com/niXman/mingw-builds-binaries/releases/download/14.2.0-rt_v12-rev1/x86_64-14.2.0-release-win32-seh-ucrt-rt_v12-rev1.7z"), 1, false)
                            .addLabeledComponent(new JBLabel(BundleUtils.i18n("debug.leetcode.my.link")), new JBTextField("https://pan.baidu.com/s/15aK7K5AIkMoMwxdV4jCNlA?pwd=1jxa"), 1, false)
                            .getPanel()
                    ,
                    BundleUtils.i18n("debug.leetcode.mingw.download.function"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{BundleUtils.i18n("action.leetcode.plugin.ok"), BundleUtils.i18n("action.leetcode.plugin.cancel")},
                    BundleUtils.i18n("action.leetcode.plugin.ok")
            );
        }
        if (i != OK_OPTION) {
            return false;
        }
        this.MINGW_HOME = myFileBrowserBtn.getText();

        this.GPP = new FileUtils.PathBuilder(this.MINGW_HOME).append("bin").append("g++.exe").build();
        this.GDB = new FileUtils.PathBuilder(this.MINGW_HOME).append("bin").append("gdb.exe").build();

        if (!FileUtils.fileExists(GPP)) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.gpp.error") + GPP);
        }
        if (!FileUtils.fileExists(GDB)) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.gdb.error") + GDB);
        }
        // 存储正确的javaPath
        StoreService.getInstance(project).addCache("MINGW_HOME", MINGW_HOME);
        return true;
    }

    /**
     * cpp的debug, 不再采用Main, Solution独立的编写方式, 不然引用太操蛋了.
     * 直接将main函数写入solution.cpp内
     * <p>
     * 此处创建的是ServerMain.cpp, 用于启动cpp的debug服务
     * @return true
     * @throws DebugError error
     */
    @Override
    protected boolean createMainFile() throws DebugError {
        String solutionExePath = new FileUtils.PathBuilder(filePath).append("solution.exe").build();
        String serverMain = FileUtils.readContentFromFile(getClass().getResource("/debug/cpp/ServerMain.template"));
        // ({{gdb_path}}, {{solution_exe_path}}, R"(--interpreter=mi2)", log);
        this.port = DebugUtils.findAvailablePort();
        /*
          存储debug服务的std log
          目前不支持目标代码的标准输出的捕获
         */
        String stdLogPath = new FileUtils.PathBuilder(filePath).append("cppLog").append("std_log.log").build();
        /*
          存储debug服务的std err
         */
        String stdErrPath = new FileUtils.PathBuilder(filePath).append("cppLog").append("std_err.log").build();

        LogUtils.simpleDebug("stdLogPath = " + stdLogPath);
        LogUtils.simpleDebug("stdErrPath = " + stdErrPath);

        // 清空文件
        try {
            FileUtils.createAndWriteFile(stdLogPath, "");
            FileUtils.createAndWriteFile(stdErrPath, "");
        } catch (IOException e) {
            LogUtils.error(e);
            throw new DebugError(BundleUtils.i18n("debug.leetcode.cpp.log.create.failed") + e.getMessage());
        }

        LogUtils.info("cpp port = " + this.port);
        serverMain = serverMain
                .replace("{{gdb_path}}", "\"" + new FileUtils.PathBuilder(this.GDB).buildWithEscape() + "\"")
                .replace("{{solution_exe_path}}", "\"" + new FileUtils.PathBuilder(solutionExePath).buildWithEscape() + "\"")
                .replace("{{port}}", String.valueOf(this.port))
                .replace("{{std_log_path}}", "\"" + new FileUtils.PathBuilder(stdLogPath).buildWithEscape() + "\"")
                .replace("{{std_err_path}}", "\"" + new FileUtils.PathBuilder(stdErrPath).buildWithEscape() + "\"")
        ;
        // 写文件
        this.serverMainPath = new FileUtils.PathBuilder(filePath).append("ServerMain.cpp").build();
        StoreService.getInstance(project).writeFile(this.serverMainPath, serverMain);
        return true;
    }

    @Override
    protected boolean createSolutionFile() throws DebugError {
        // 获取路径
        String solutionPath = new FileUtils.PathBuilder(filePath).append("solution.cpp").build();
        this.solutionCppPath = solutionPath;
        String solutionContent = getSolutionContent();
        // 获取main函数
        solutionContent += "\n" + getMainFunction();
        // 写文件
        StoreService.getInstance(project).writeFile(solutionPath, solutionContent);
        return true;
    }

    private String getMainFunction() {
        // 读取Main.template
        String mainContent = FileUtils.readContentFromFile(getClass().getResource("/debug/cpp/Main.template"));
        // 获取callCode
        mainContent = mainContent.replace("{{callCode}}", getCallCode());
        // 存储文件
        StoreService.getInstance(project).writeFile(this.solutionCppPath, mainContent);
        return mainContent;
    }

    private CharSequence getCallCode() {
        // 分析得到代码片段
        CppCodeAnalyzer analyzer = new CppCodeAnalyzer(project);
        AnalysisResult result = analyzer.autoAnalyze();
        this.methodName = result.getMethodName();
        CppTestcaseConvertor convertor = new CppTestcaseConvertor("solution", result, project);
        // 得到调用代码
        return convertor.autoConvert();
    }

    private String getSolutionContent() {
        String content = ViewUtils.getContentOfCurrentOpenVFile(project);
        if (content == null) {
            throw new DebugError(BundleUtils.i18nHelper("当前打开文件为空", "current file is empty"));
        }
        String include = "#include \"leetcode.h\"";
        // 目前不需要增加偏移量
        this.offset = 0;
        content = include + content;
        return content;
    }

    /**
     * 支持用户cancel进程
     */
    public static class MyTask extends Task.WithResult<Integer, Exception> {

        private final String combinedCmd;
        private Process process;

        public MyTask(Project project, String combineCmd) {
            super(project,
                    BundleUtils.i18nHelper(
                            "debug服务编译中, 需要一点时间, 这个时候, 您可以打开手机, 原生, 启动!",
                            "debug service is compiling, please wait a moment, you can open the mobile phone, 原生, start!"
                    )
                    , true
            );
            this.combinedCmd = combineCmd;
        }

        @Override
        protected Integer compute(@NotNull ProgressIndicator indicator) throws Exception {
            LogUtils.simpleDebug("compile combinedCmd = " + combinedCmd);
            /*
              这里之所以使用全新线程执行combinedCmd, 是因为它会阻塞线程, 导致用户点击cancel后
              无法触发onCancel方法, 所以使用新线程执行, 并且在while循环中判断是否被cancel
             */
            new Thread(() -> {
                try {
                    process = DebugUtils.buildProcess("cmd.exe", "/c", combinedCmd);
                    DebugUtils.printProcess(process, true, super.myProject);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            while (! indicator.isCanceled()) {
                Thread.sleep(300);
                try {
                    if (process != null) {
                        int exitProcess = process.exitValue();
                        return exitProcess;
                    }
                } catch (IllegalThreadStateException e) {
                    // 进程尚未结束，继续等待
                }
            }

            return null;
        }

        @Override
        public void onCancel() {
            if (process != null && process.isAlive()) {
                process.destroy(); // 尝试正常终止进程
                // process.destroyForcibly();
                LogUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.compile.stop"));
            }
        }
    }

    private boolean buildFile() {
        try {
            // 指定生成exe文件的绝对路径, 否则会出现一堆奇葩错误, md
            String cmd = GPP + " -g " + this.solutionCppPath + " -o " + this.solutionExePath;
            String cmd2 = GPP + " -g " + this.serverMainPath + " -lws2_32 -o " + this.serverMainExePath;

            String combinedCmd = cmd + " & " + cmd2;

            Integer i = ProgressManager.getInstance().run(new MyTask(project, combinedCmd));

            if (i == null) {
                LogUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.compile.cancel"));
                ConsoleUtils.getInstance(project).showError(BundleUtils.i18nHelper("取消编译!", "cancel compile!"), false, true);
                return false;
            }

            if (i != 0) {
                throw new DebugError(BundleUtils.i18n("debug.leetcode.compile.error") + "\n" +
                        "solution.exe = " + this.solutionExePath+
                        "ServerMain.exe = " + this.serverMainExePath
                );
            }
            return true;
        } catch (Exception e) {
            throw new DebugError(e.getMessage(), e);
        }
    }

    public String getGDB() {
        return GDB;
    }

    public String getGPP() {
        return GPP;
    }

    public String getMINGW_HOME() {
        return MINGW_HOME;
    }

    public String getSolutionCppPath() {
        return solutionCppPath;
    }

    public int getPort() {
        return this.port;
    }

    public int getOffset() {
        return offset;
    }

    public String getServerMainPath() {
        return serverMainPath;
    }

    public String getSolutionExePath() {
        return solutionExePath;
    }

    public String getServerMainExePath() {
        return serverMainExePath;
    }

    public String getMethodName() {
        return this.methodName;
    }
}
