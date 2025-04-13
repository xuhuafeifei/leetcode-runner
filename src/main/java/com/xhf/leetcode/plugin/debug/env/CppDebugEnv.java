package com.xhf.leetcode.plugin.debug.env;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
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
import com.xhf.leetcode.plugin.utils.OSHandler;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

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
     * solution.exe的路径
     */
    private String solutionExePath;
    /**
     * ServerMain.exe的路径
     * 在v3.7.0中, 取消了ServerMain.exe的编译, 直接使用已经在不同OS编译好的文件, 文件名均以leetcode_runner_debug_server_cpp_xxx.xxx命名
     */
    private String serverMainExePath;
    private String stdLogPath;
    private String stdErrPath;

    public CppDebugEnv(Project project) {
        super(project);
    }

    @Override
    protected void initFilePath() {
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").append("cpp").build();
    }

    @Override
    public boolean prepare() throws DebugError {
        return buildToolPrepare() && testcasePrepare() && createSolutionFile() && createMainFile() && closeExe() && copyFile() && buildFile();
    }

    /**
     * 强制关闭exe, 该方法不论是否执行成功, 都不会返回false
     * @return boolean
     */
    private boolean closeExe() {
        this.solutionExePath = new FileUtils.PathBuilder(filePath).append("solution.exe").build();
        this.serverMainExePath = new FileUtils.PathBuilder(filePath).append(getCompiledFileName()).build();

        try {
            FileUtils.removeFile(this.solutionExePath);
            LogUtils.simpleDebug(this.solutionExePath + BundleUtils.i18nHelper(" 删除成功!", " delete failed!"));
        } catch (Exception e) {
            DebugUtils.simpleDebug(this.solutionExePath + BundleUtils.i18nHelper(" 删除失败!", " delete failed!") + " " + BundleUtils.i18n("action.leetcode.plugin.cause") +  " = " + e.getMessage(), project, ConsoleViewContentType.ERROR_OUTPUT);
        }
        try {
            FileUtils.removeFile(this.serverMainExePath);
            LogUtils.simpleDebug(this.solutionExePath + BundleUtils.i18nHelper(" 删除成功!", " delete failed!"));
        } catch (Exception e) {
            DebugUtils.simpleDebug(this.serverMainExePath + BundleUtils.i18nHelper(" 删除失败!", " delete failed!") + " cause = " + e.getMessage(), project, ConsoleViewContentType.ERROR_OUTPUT);
        }
        return true;
    }

    private String getCompiledFile() {
        return OSHandler.chooseCompliedFile(
                "/debug/cpp/complie/windows/leetcode_runner_debug_server_cpp_windows.exe",
                "/debug/cpp/complie/linux/leetcode_runner_debug_server_cpp_linux.out"
        );
    }

    private String getCompiledFileName() {
        return OSHandler.chooseCompliedFile(
                "leetcode_runner_debug_server_cpp_windows.exe",
                "leetcode_runner_debug_server_cpp_linux.out"
        );
    }

    @Override
    protected boolean copyFile() {
        return copyFileHelper(getCompiledFile())
               && copyFileHelper("/debug/cpp/leetcode.h") ;
    }

    @Override
    protected boolean buildToolPrepare() throws DebugError {
        if (OSHandler.isWin()) {
            return buildToolPrepareForWin();
        } else {
            return buildToolPrepareForMac();
        }
    }

    private boolean buildToolPrepareForMac() {
        boolean GPP_FLAG = StoreService.getInstance(project).contains("GPP");
        boolean GDB_FLAG = StoreService.getInstance(project).contains("GDB");

        // gpp路径选择BTN
        TextFieldWithBrowseButton gppBtn = new TextFieldWithBrowseButton();
        gppBtn.addBrowseFolderListener(
                new TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                ) {
                });

        // gdb路径选择BTN
        TextFieldWithBrowseButton gdbBtn = new TextFieldWithBrowseButton();
        gdbBtn.addBrowseFolderListener(
                new TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                ) {
                });

        // 判断是否有缓存
        if (GPP_FLAG) {
            gppBtn.setText(StoreService.getInstance(project).getCache("GPP", String.class));
        }
        if (GDB_FLAG) {
            gdbBtn.setText(StoreService.getInstance(project).getCache("GDB", String.class));
        }

        // 弹框选择
        JPanel targetComponent = new JPanel();
        targetComponent.add(
                FormBuilder.createFormBuilder()
                        .addLabeledComponent(new JBLabel(BundleUtils.i18n("debug.leetcode.gpp.path")), gppBtn, 1, false)
                        .addLabeledComponent(new JBLabel(BundleUtils.i18n("debug.leetcode.gdb.path")), gdbBtn, 1, false)
                        .getPanel()
        );

        int i = ViewUtils.getDialogWrapper(
                targetComponent,
                BundleUtils.i18nHelper("选择GPP和GDB路径", "choose GPP and GDB path")
        ).getExitCode();

        if (i != DialogWrapper.OK_EXIT_CODE) {
            return false;
        }

        // 校验路径GPP
        this.GPP = gppBtn.getText();
        if (! OSHandler.isGPP(this.GPP)) {
            throw new DebugError(BundleUtils.i18nHelper("GPP路径错误: " + this.GPP, "GPP path error: " + this.GPP));
        }

        // 校验路径GDB
        this.GDB = gdbBtn.getText();
        if (! OSHandler.isGDB(this.GDB)) {
            throw new DebugError(BundleUtils.i18nHelper("GDB路径错误: " + this.GDB, "GDB path error: " + this.GDB));
        }

        if (!FileUtils.fileExists(GPP)) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.gpp.error") + GPP);
        }
        if (!FileUtils.fileExists(GDB)) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.gdb.error") + GDB);
        }

        // 存储正确的javaPath
        StoreService.getInstance(project).addCache("GPP", GPP);
        StoreService.getInstance(project).addCache("GDB", GDB);
        return true;
    }

    private boolean buildToolPrepareForWin() {
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

        String cppPath;
        if (flag) {
            cppPath = StoreService.getInstance(project).getCache("MINGW_HOME", String.class);
            myFileBrowserBtn.setText(cppPath);
        }

        int MiNGW_EXIT_CODE = 999;

        int i = ViewUtils.getDialogWrapper(
                targetComponent,
                BundleUtils.i18nHelper("选择MinGW目录", "choose MinGW directory"),
                new String[] {BundleUtils.i18n("debug.leetcode.main.mingw.no")},
                new int[] {MiNGW_EXIT_CODE}
        ).getExitCode();

        if (i == MiNGW_EXIT_CODE) {
            // 给出下载链接
            ViewUtils.getDialogWrapper(
                    FormBuilder.createFormBuilder()
                            .addLabeledComponent(new JBLabel(BundleUtils.i18n("debug.leetcode.github.link")), new JBTextField("https://github.com/niXman/mingw-builds-binaries/releases/download/14.2.0-rt_v12-rev1/x86_64-14.2.0-release-win32-seh-ucrt-rt_v12-rev1.7z"), 1, false)
                            .addLabeledComponent(new JBLabel(BundleUtils.i18n("debug.leetcode.my.link")), new JBTextField("https://pan.baidu.com/s/15aK7K5AIkMoMwxdV4jCNlA?pwd=1jxa"), 1, false)
                            .getPanel()
                    ,
                    BundleUtils.i18n("debug.leetcode.mingw.download.function")
            );
        }
        if (i != DialogWrapper.OK_EXIT_CODE) {
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
     * 创建Main.cpp文件 + leetcode.h文件
     * leetcode.h文件中包含了gdb_path, solution_exe_path, port, std_log_path, std_err_path
     * Main.cpp文件中包含了用户编写的代码, 以及调用代码
     * <p>
     * cpp debug采用预编译的形式启动项目. 由feigebuge在各个平台提前编译Cpp Server, 并放入项目目录中.
     *
     * @return true
     * @throws DebugError error
     */
    @Override
    protected boolean createMainFile() throws DebugError {
        // ({{gdb_path}}, {{solution_exe_path}}, R"(--interpreter=mi2)", log);
        this.port = DebugUtils.findAvailablePort();
        /*
          存储debug服务的std log
          目前不支持目标代码的标准输出的捕获
         */
        this.stdLogPath = new FileUtils.PathBuilder(filePath).append("cppLog").append("std_log.log").build();
        /*
          存储debug服务的std err
         */
        this.stdErrPath = new FileUtils.PathBuilder(filePath).append("cppLog").append("std_err.log").build();

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
        return true;
    }

    public String[] getServerArgv() {
        return new String[]{
                String.valueOf(this.port),
                new FileUtils.PathBuilder(stdLogPath).build(),
                new FileUtils.PathBuilder(stdErrPath).build(),
                new FileUtils.PathBuilder(this.GDB).build(),
                new FileUtils.PathBuilder(solutionExePath).build()
        };
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

            DebugUtils.simpleDebug(cmd, project);

            Integer i = ProgressManager.getInstance().run(new MyTask(project, cmd));

            if (i == null) {
                LogUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.compile.cancel"));
                ConsoleUtils.getInstance(project).showError(BundleUtils.i18nHelper("取消编译!", "cancel compile!"), false, true);
                return false;
            }

            if (i != 0) {
                throw new DebugError(BundleUtils.i18n("debug.leetcode.compile.error") + "\n" +
                        OSHandler.chooseCompliedFile("solution.exe", "solution.out") + " = " + this.solutionExePath + "\n" +
                        getCompiledFileName() + " = " + this.serverMainExePath
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
