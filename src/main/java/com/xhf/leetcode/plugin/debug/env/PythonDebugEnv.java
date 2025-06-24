package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.PythonCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.converter.PythonTestcaseConvertor;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugConfig;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.setting.InnerHelpTooltip;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.OSHandler;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import java.io.IOException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugEnv extends AbstractDebugEnv {
    /**
     * python debugger的启动配置
     */
    private final PythonDebugConfig config;
    /**
     * python执行器路径
     */
    private String python;
    /**
     * main.py路径
     */
    private String mainPyPath;
    /**
     * 偏移量, 用于调试时对齐用户使用idea显示的代码
     */
    private int offset;
    /**
     * solution.py路径
     */
    private String solutionPyPath;
    /**
     * python服务启动端口
     */
    private int pyPort;
    private String logDir;
    private String stdOutDir;
    private String stdErrDir;

    public PythonDebugEnv(Project project, PythonDebugConfig config) {
        super(project);
        this.config = config;
    }

    @Override
    protected void initFilePath() {
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").append("python").build();
    }

    @Override
    public boolean prepare() throws DebugError {
        return buildToolPrepare() && testcasePrepare() && createSolutionFile() && createMainFile() && copyFile();
    }

    /**
     * copy python需要的代码
     * @return
     */
    @Override
    protected boolean copyFile() {
        // copy
        return
//                copyFileHelper("/debug/python/inst_source.py") &&
//                copyFileHelper("/debug/python/log_out_helper.py") &&
//                copyFileHelper("/debug/python/debug_core.py") &&
//                copyFileHelper("/debug/python/execute_result.py") &&
//                copyFileHelper("/debug/python/server.py") &&
//                copyFileHelper("/debug/python/ListNode.py") &&
//                copyFileHelper("/debug/python/TreeNode.py")
                copyFileExcept("/debug/python",
                        new String[]{
                                "ListNodeConvertor.template",
                                "TreeNodeConvertor.template",
                                "test.cmd",
                                "Main.template",
                                "Main.py",
                                "Solution.py"
                        }
                );
    }

    @Override
    protected boolean buildToolPrepare() throws DebugError {
        boolean flag = StoreService.getInstance(project).contains("PYTHON_EXE");

        TextFieldWithBrowseButton myFileBrowserBtn = new TextFieldWithBrowseButton();
        myFileBrowserBtn.addBrowseFolderListener(ViewUtils.getBrowseFolderListener(myFileBrowserBtn));

        String javaPath;
        if (flag) {
            javaPath = StoreService.getInstance(project).getCache("PYTHON_EXE", String.class);
            myFileBrowserBtn.setText(javaPath);
        }

        int i = ViewUtils.getDialogWrapper(
                InnerHelpTooltip
                        .BoxLayout()
                        .add(myFileBrowserBtn)
                        .addHelp(BundleUtils.i18n("debug.leetcode.python.home.path.tip"))
                        .getTargetComponent()
                ,
                BundleUtils.i18nHelper("设置python解释器路径", "Set python interpreter path")
        ).getExitCode();

        if (i != DialogWrapper.OK_EXIT_CODE) {
            return false;
        }

        String pythonPath = myFileBrowserBtn.getText();
        boolean exist = OSHandler.isPythonInterpreter(pythonPath);
        if (!exist) {
            throw new DebugError(BundleUtils.i18nHelper("python解释器路径错误! " + pythonPath, "Invalid python interpreter path! ") + pythonPath);
        }

        python = pythonPath;

        if (!FileUtils.fileExists(python)) {
            throw new DebugError(OSHandler.getPythonName() + BundleUtils.i18n("action.leetcode.plugin.path.error") + python);
        }
        // 存储正确的javaPath
        StoreService.getInstance(project).addCache("PYTHON_EXE", pythonPath);
        return true;
    }

    @Override
    protected boolean createMainFile() throws DebugError {
        String mainPath = new FileUtils.PathBuilder(filePath).append("Main.py").build();
        this.mainPyPath = mainPath;
        // 读取Main.template
        String mainContent = FileUtils.readContentFromFile(getClass().getResource("/debug/python/Main.template"));
        // 获取callCode
        this.pyPort = DebugUtils.findAvailablePort();
        String callCode = getCallCode();
        this.logDir = new FileUtils.PathBuilder(filePath).append("pylog").append("run.log").buildUnUnify();
        this.stdOutDir = new FileUtils.PathBuilder(filePath).append("pylog").append("std_out.log").buildUnUnify();
        this.stdErrDir = new FileUtils.PathBuilder(filePath).append("pylog").append("std_err.log").buildUnUnify();

        LogUtils.simpleDebug("logDir = " + this.logDir);
        LogUtils.simpleDebug("stdOutDir = " + this.stdOutDir);
        LogUtils.simpleDebug("stdErrDir = " + this.stdErrDir);

        // 清空文件
        try {
            FileUtils.removeFile(this.logDir);
            FileUtils.removeFile(this.stdOutDir);
            FileUtils.removeFile(this.stdErrDir);

            FileUtils.createAndWriteFile(this.logDir, "");
            FileUtils.createAndWriteFile(this.stdOutDir, "");
            FileUtils.createAndWriteFile(this.stdErrDir, "");
        } catch (IOException e) {
            LogUtils.error(e);
            throw new DebugError(BundleUtils.i18nHelper("python日志文件创建错误!", "Failed to create python log files!") + e);
        }

        mainContent = mainContent.replace("{{callCode}}", callCode)
                .replace("{{port}}", String.valueOf(this.pyPort))
                .replace("{{methodName}}", "\"" + this.methodName + "\"")
                .replace("{{read_type}}", "\"" + config.getReadType().getType() + "\"")
                .replace("{{log_dir}}", "\"" + this.logDir + "\"")
                .replace("{{std_out_dir}}", "\"" + this.stdOutDir + "\"")
                .replace("{{std_err_dir}}", "\"" + this.stdErrDir + "\"")
                .replace("{{language_type}}", "\"" + AppSettings.getInstance().getI18nType().getValue() + "\"")
        ;
        // debug
        DebugUtils.simpleDebug(BundleUtils.i18nHelper("python服务端口确定: ", "python service port determined: ") + pyPort, project);
        DebugUtils.simpleDebug(BundleUtils.i18nHelper("核心调用代码: \n", "core call code: \n") + callCode, project);
        DebugUtils.simpleDebug(BundleUtils.i18nHelper("方法名: ", "methodName: ") + methodName, project);
        // 存储文件
        StoreService.getInstance(project).writeFile(mainPath, mainContent);
        return true;
    }

    private String getCallCode() {
        // 分析得到代码片段
        PythonCodeAnalyzer analyzer = new PythonCodeAnalyzer(project);
        AnalysisResult result = analyzer.autoAnalyze();
        this.methodName = result.getMethodName();
        PythonTestcaseConvertor convertor = new PythonTestcaseConvertor("solution", result, project);
        // 得到调用代码
        return convertor.autoConvert();
    }

    @Override
    protected boolean createSolutionFile() throws DebugError {
        // 获取路径
        String solutionPath = new FileUtils.PathBuilder(filePath).append("Solution.py").build();
        this.solutionPyPath = solutionPath;
        String solutionContent = getSolutionContent();
        // 写文件
        StoreService.getInstance(project).writeFile(solutionPath, solutionContent);
        return true;
    }

    private String getSolutionContent() {
        String content = ViewUtils.getContentOfCurrentOpenVFile(project);
        if (content == null) {
            throw new DebugError(BundleUtils.i18nHelper("当前打开文件为空", "Current open file is empty!"));
        }
        // 替换含有package的那一行为空, 只留下换行符
        if (content.startsWith("package")) {
            content = content.replaceFirst("^package .*", "");
        }
        /*
          通用包
         */
        String commonPackage =
                "# Common imports for LeetCode problems\n" +
                "from typing import List, Optional, Tuple, Dict, Set\n" +
                "from collections import defaultdict, Counter, deque, namedtuple, OrderedDict\n" +
                "from heapq import heappush, heappop, heapify\n" +
                "from functools import *\n" +
                "import operator\n" +
                "from itertools import permutations, combinations, product, accumulate\n" +
                "from bisect import bisect_left, bisect_right\n" +
                "import math\n" +
                "import sys\n" +
                "from ListNode import ListNode\n" +
                "from TreeNode import TreeNode\n" +
                "from math import *\n"
                ;
        // 设置偏移量, 后期debug的时候需要通过offset对齐
        this.offset = commonPackage.split("\n").length;
        return commonPackage + content;
    }

    public String getPython() {
        return python;
    }

    public String getMainPyPath() {
        return mainPyPath;
    }

    public int getOffset() {
        return offset;
    }

    public String getSolutionPyPath() {
        return solutionPyPath;
    }

    public int getPyPort() {
        return pyPort;
    }

    public String getLogDir() {
        return logDir;
    }

    public String getStdOutDir() {
        return stdOutDir;
    }

    public String getStdErrDir() {
        return stdErrDir;
    }
}
