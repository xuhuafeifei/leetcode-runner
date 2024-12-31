package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
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
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import javax.swing.*;

import java.io.IOException;

import static javax.swing.JOptionPane.*;

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
                // 别在屁股后面加/
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
        myFileBrowserBtn.addBrowseFolderListener(
                new TextBrowseFolderListener(
                        FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                ) {
                });
        String javaPath;
        if (flag) {
            javaPath = StoreService.getInstance(project).getCache("PYTHON_EXE", String.class);
            myFileBrowserBtn.setText(javaPath);
        }

        int i = JOptionPane.showOptionDialog(
                null,
                myFileBrowserBtn,
                "选择python解释器的路径",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"确定", "取消"},
                "确定"
        );
        if (i != OK_OPTION) {
            return false;
        }
        python = myFileBrowserBtn.getText();

        if (!FileUtils.fileExists(python)) {
            throw new DebugError("python解释器路径错误 = " + python);
        }
        // 存储正确的javaPath
        StoreService.getInstance(project).addCache("PYTHON_EXE", python);
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
            FileUtils.createAndWriteFile(this.logDir, "");
            FileUtils.createAndWriteFile(this.stdOutDir, "");
            FileUtils.createAndWriteFile(this.stdErrDir, "");
        } catch (IOException e) {
            LogUtils.error(e);
            throw new DebugError("python日志文件创建错误!" + e.toString());
        }

        mainContent = mainContent.replace("{{callCode}}", callCode)
                .replace("{{port}}", String.valueOf(this.pyPort))
                .replace("{{methodName}}", "\"" + this.methodName + "\"")
                .replace("{{read_type}}", "\"" + config.getReadType().getType() + "\"")
                .replace("{{log_dir}}", "\"" + this.logDir + "\"")
                .replace("{{std_out_dir}}", "\"" + this.stdOutDir + "\"")
                .replace("{{std_err_dir}}", "\"" + this.stdErrDir + "\"")
        ;
        // debug
        DebugUtils.simpleDebug("python服务端口确定: " + pyPort, project);
        DebugUtils.simpleDebug("核心调用代码: \n" + callCode, project);
        DebugUtils.simpleDebug("methodName: " + methodName, project);
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
            throw new DebugError("当前打开文件为空");
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
                "from functools import lru_cache, cache\n" +
                "from itertools import permutations, combinations, product, accumulate\n" +
                "from bisect import bisect_left, bisect_right\n" +
                "import math\n" +
                "import sys\n" +
                "from ListNode import ListNode\n" +
                "from TreeNode import TreeNode\n"
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
