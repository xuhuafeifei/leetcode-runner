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
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.setting.InnerHelpTooltip;
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
    /**
     * 存储debug服务的std err
     */
    private String stdErrPath;
    /**
     * 存储debug服务的std log
     */
    private String stdLogPath;

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
            FileUtils.removeFile(this.serverMainExePath);
        } catch (Exception ignored) {
            DebugUtils.simpleDebug("删除solution.exe失败: " + this.solutionExePath + " cause = " + ignored.getCause(), project, ConsoleViewContentType.ERROR_OUTPUT);
        }
        return true;
    }

    @Override
    protected boolean copyFile() {
        // return copyFileHelper("/debug/cpp/leetcode.h");
        return
                copyFileExcept("/debug/cpp",
                        new String[]{
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
        String HELP_CONTENT = "<p><strong>MinGW</strong>是一个用于 <strong>Windows</strong> 平台的开发工具集，其中包含如<strong>g++</strong>、<strong>gcc</strong>、<strong>gdb</strong>等<strong>debug</strong>使用的核心工具</p>\n" +
                "<p>而leetcode-runner的c++ debug运行依赖于<strong>MinGW</strong>提供的工具, 因此需要使用者单独下载MinGW</p>\n" +
                "<p>另外, 建议使用和项目适配的MinGW, 具体下载链接您可以通过点击<code>没有MinGW?</code>按钮获取</p>" +
                "<p>如果使用其他版本的MinGW, 可能会出现意料之外的异常</p>"
                ;
        JPanel targetComponent = null;
        targetComponent = InnerHelpTooltip.BoxLayout().add(myFileBrowserBtn).addHelp(HELP_CONTENT).getTargetComponent();

        String javaPath;
        if (flag) {
            javaPath = StoreService.getInstance(project).getCache("MINGW_HOME", String.class);
            myFileBrowserBtn.setText(javaPath);
        }

        int i = JOptionPane.showOptionDialog(
                null,
                targetComponent,
                "选择MinGW目录",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"确定", "取消", "没有MinGW?"},
                "确定"
        );
        if (i == 2) {
            // 给出下载链接
            JOptionPane.showOptionDialog(
                    null,
                    FormBuilder.createFormBuilder()
                            .addLabeledComponent(new JBLabel("Github下载链接: "), new JBTextField("https://github.com/niXman/mingw-builds-binaries/releases/download/14.2.0-rt_v12-rev1/x86_64-14.2.0-release-win32-seh-ucrt-rt_v12-rev1.7z"), 1, false)
                            .addLabeledComponent(new JBLabel("Fgbg提供的链接: "), new JBTextField("https://pan.baidu.com/s/15aK7K5AIkMoMwxdV4jCNlA?pwd=1jxa"), 1, false)
                            .getPanel()
                    ,
                    "MinGW的两种下载方式",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{"确定", "取消"},
                    "确定"
            );
        }
        if (i != OK_OPTION) {
            return false;
        }
        this.MINGW_HOME = myFileBrowserBtn.getText();

        this.GPP = new FileUtils.PathBuilder(this.MINGW_HOME).append("bin").append("g++.exe").build();
        this.GDB = new FileUtils.PathBuilder(this.MINGW_HOME).append("bin").append("gdb.exe").build();

        if (!FileUtils.fileExists(GPP)) {
            throw new DebugError("无法找到g++.exe, g++路径错误 = " + GPP);
        }
        if (!FileUtils.fileExists(GDB)) {
            throw new DebugError("无法找到gdb.exe, gdb路径错误 = " + GDB);
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
        this.stdLogPath = new FileUtils.PathBuilder(filePath).append("cppLog").append("std_log.log").build();
        this.stdErrPath = new FileUtils.PathBuilder(filePath).append("cppLog").append("std_log.log").build();

        LogUtils.simpleDebug("stdLogPath = " + this.stdLogPath);
        LogUtils.simpleDebug("stdErrPath = " + this.stdErrPath);

        // 清空文件
        try {
            FileUtils.createAndWriteFile(this.stdLogPath, "");
            FileUtils.createAndWriteFile(this.stdErrPath, "");
        } catch (IOException e) {
            LogUtils.error(e);
            throw new DebugError("python日志文件创建错误!" + e.toString());
        }

        LogUtils.info("cpp port = " + this.port);
        serverMain = serverMain
                .replace("{{gdb_path}}", "\"" + new FileUtils.PathBuilder(this.GDB).buildWithEscape() + "\"")
                .replace("{{solution_exe_path}}", "\"" + new FileUtils.PathBuilder(solutionExePath).buildWithEscape() + "\"")
                .replace("{{port}}", String.valueOf(this.port))
                .replace("{{std_log_path}}", "\"" + new FileUtils.PathBuilder(this.stdLogPath).buildWithEscape() + "\"")
                .replace("{{std_err_path}}", "\"" + new FileUtils.PathBuilder(this.stdErrPath).buildWithEscape() + "\"")
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
            throw new DebugError("当前打开文件为空");
        }
        String include = "#include \"leetcode.h\"";
        // 目前不需要增加偏移量
        this.offset = 0;
        content = include + content;
        return content;
    }

    public class MyTask extends Task.WithResult<Integer, Exception> {

        private final String combinedCmd;
        private Process process;

        public MyTask(Project project, String combineCmd) {
            super(project, "debug服务编译中, 需要一点时间, 这个时候, 您可以打开手机, 原生, 启动!", true);
            this.combinedCmd = combineCmd;
        }

        @Override
        protected Integer compute(@NotNull ProgressIndicator indicator) throws Exception {
            LogUtils.simpleDebug("编译combinedCmd = " + combinedCmd);
            process = Runtime.getRuntime().exec(combinedCmd);
            process.waitFor();

            return process.exitValue();
        }

        @Override
        public void onCancel() {
            if (process != null && process.isAlive()) {
                process.destroy(); // 尝试正常终止进程
                // 如果需要强制终止，可以使用 destroyForcibly()
                // process.destroyForcibly();
                System.out.println("用户取消了任务，进程已终止");
            }
        }
    }

    private boolean buildFile() {
        try {
            String cdCmd = "cd " + this.filePath;
            String cmd = GPP + " -g " + this.solutionCppPath + " -o solution.exe";
            String cmd2 = GPP + " -g " + this.serverMainPath + " -lws2_32 -o ServerMain.exe";

            String combinedCmd = " cmd /c " + cdCmd + " & " + cmd + " & " + cmd2;

            Integer i = ProgressManager.getInstance().run(new MyTask(project, combinedCmd));

//            Integer i = ProgressManager.getInstance().runProcessWithProgressSynchronously(
//                    (ThrowableComputable<Integer, Exception>) () -> {
//                        LogUtils.simpleDebug("编译combinedCmd = " + combinedCmd);
//                        Process exec = Runtime.getRuntime().exec(combinedCmd);
//
//                        DebugUtils.printProcess(exec, false, project);
//                        return exec.exitValue();
//                    },
//                    "debug服务编译中, 需要一点时间, 这个时候, 您可以打开手机, 原生, 启动!",
//                    true,
//                    project
//            );

            if (i != 0) {
                throw new DebugError("编译文件异常, 详细信息可查看Console, 如果在控制台发现ServerMain.exe无法被删除, 请您手动删除他\nServerMain.exe路径=" + this.serverMainExePath);
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
