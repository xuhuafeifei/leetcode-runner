package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.converter.JavaTestcaseConvertor;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import javax.swing.*;

import static javax.swing.JOptionPane.OK_OPTION;

/**
 * 启动Java环境的debug
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugEnv extends AbstractDebugEnv {
    /**
     * Java编写的Main类路径
     */
    private String mainJavaPath = "E:\\java_code\\lc-test\\cache\\debug\\Main.java";
    /**
     * Java编写的Solution类的路径
     */
    private String solutionJavaPath = "E:\\java_code\\lc-test\\cache\\debug\\Solution.java";
    /**
     * 编译后的Main.class路径
     */
    private String mainClassPath = "E:\\java_code\\lc-test\\cache\\debug\\Main.class";
    /**
     * JAVA_HOME
     */
    private String JAVA_HOME = "E:\\jdk8";
    /**
     * java执行路径
     */
    private String java = "E:\\jdk8\\bin\\java.exe";
    /**
     * javac执行路径
     */
    private String javac = "E:\\jdk8\\bin\\javac.exe";


    public JavaDebugEnv(Project project) {
        super(project);
    }

    @Override
    protected void initFilePath() {
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").append("java").build();
    }

    @Deprecated // only for test
    public JavaDebugEnv() {
        super(null);
    }

    /**
     * 构建主类 + cv当前打开的文件
     */
    @Override
    public boolean prepare() throws DebugError {
        return buildToolPrepare() && testcasePrepare() && createSolutionFile() && createMainFile() && copyFile() && buildFile();
    }

    @Override
    protected boolean copyFile() {
        return
                // 不要拷贝/debug/java/test目录下的任何内容
                copyFileHelper("/debug/java/ListNode.java") &&
                copyFileHelper("/debug/java/TreeNode.java");
    }

    /**
     * 获取编译工具路径
     * @return
     */
    @Override
    protected boolean buildToolPrepare() throws DebugError{
        boolean flag = StoreService.getInstance(project).contains("JAVA_HOME");

        TextFieldWithBrowseButton myFileBrowserBtn = new TextFieldWithBrowseButton();
        myFileBrowserBtn.addBrowseFolderListener(
                new TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                ) {
                });
        String javaPath;
        if (flag) {
            javaPath = StoreService.getInstance(project).getCache("JAVA_HOME", String.class);
            myFileBrowserBtn.setText(javaPath);
        }

        int i = JOptionPane.showOptionDialog(
                null,
                myFileBrowserBtn,
                "选择Java目录",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{BundleUtils.i18n("action.leetcode.plugin.ok"), BundleUtils.i18n("action.leetcode.plugin.cancel")},
                BundleUtils.i18n("action.leetcode.plugin.ok")
        );
        if (i != OK_OPTION) {
                return false;
        }
        JAVA_HOME = myFileBrowserBtn.getText();

        java = new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("java.exe").build();
        javac = new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("javac.exe").build();
        if (!FileUtils.fileExists(java)) {
            throw new DebugError("Java" + BundleUtils.i18n("action.leetcode.plugin.path.error") + " = " + java);
        }
        if (!FileUtils.fileExists(javac)) {
            throw new DebugError("Javac" + BundleUtils.i18n("action.leetcode.plugin.path.error") + " = " + javac);
        }
        // 存储正确的javaPath
        StoreService.getInstance(project).addCache("JAVA_HOME", JAVA_HOME);
        return true;
    }

    private boolean buildFile() throws DebugError{
        // 通过java编译mainJavaPath下的Java类
        try {
            // 获取系统javac路径
            String cdCmd = "cd " + this.filePath;
            String cmd = javac + " -g -encoding UTF-8 -cp " + this.filePath + " " + mainJavaPath + "  -Xdiags:verbose";

            String combinedCmd = " cmd /c " + cdCmd + " & " + cmd;

            LogUtils.simpleDebug("compile cmd = " + combinedCmd);
            Process exec = DebugUtils.buildProcess("cmd.exe", "/c", cdCmd + " & " + cmd);
            DebugUtils.printProcess(exec, false, project);

            int i = exec.exitValue();
            if (i != 0) {
                throw new DebugError(BundleUtils.i18n("debug.leetcode.compile.java.error"));
            }
            mainClassPath = mainJavaPath.replace("Main.java", "Main.class");
            return true;
        } catch (Exception e) {
            throw new DebugError(e.getMessage(), e);
        }
    }

    @Override
    protected boolean createMainFile() throws DebugError {
        String mainPath = new FileUtils.PathBuilder(filePath).append("Main.java").build();
        this.mainJavaPath = mainPath;
        // 读取Main.template
        String mainContent = FileUtils.readContentFromFile(getClass().getResource("/debug/java/Main.template"));
        // 获取callCode
        mainContent = mainContent.replace("{{callCode}}", getCallCode());
        // 存储文件
        StoreService.getInstance(project).writeFile(mainPath, mainContent);
        return true;
    }

    private String getCallCode() throws DebugError {
        // 分析得到代码片段
        JavaCodeAnalyzer analyzer = new JavaCodeAnalyzer(project);
        AnalysisResult result = analyzer.autoAnalyze();
        this.methodName = result.getMethodName();
        JavaTestcaseConvertor convertor = new JavaTestcaseConvertor("solution", result, project);
        // 得到调用代码
        return convertor.autoConvert();
    }

    @Override
    protected boolean createSolutionFile() {
        // 获取路径
        String solutionPath = new FileUtils.PathBuilder(filePath).append("Solution.java").build();
        this.solutionJavaPath = solutionPath;
        String solutionContent = getSolutionContent();
        // 写文件
        StoreService.getInstance(project).writeFile(solutionPath, solutionContent);
        return true;
    }

    private String getSolutionContent() {
        String content = ViewUtils.getContentOfCurrentOpenVFile(project);
        if (content == null) {
            throw new DebugError("当前打开文件为空");
        }
        // 替换含有package的那一行
        if (content.startsWith("package")) {
            // 如果有package语句，替换为import java.util.*
            // import语句不能加换行符, 否则打印行号对不上, 除非减去offset. 不过Java这块就不这么做了, 毕竟只要有分号, 一行可以解决很多包引入内容)
            content = content.replaceFirst("^package .*;", "import java.util.*;");
        } else {
            // 如果没有package语句，直接添加import语句到第一行
            content = "import java.util.*;" + content;
        }
        return content;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMainJavaPath() {
        return mainJavaPath;
    }

    public String getSolutionJavaPath() {
        return solutionJavaPath;
    }

    public String getMainClassPath() {
        return mainClassPath;
    }

    public String getJava() {
        return java;
    }

    public String getJavac() {
        return javac;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getJAVA_HOME() {
        return JAVA_HOME;
    }
}
