package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.PythonCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.converter.JavaTestcaseConvertor;
import com.xhf.leetcode.plugin.debug.analysis.converter.PythonTestcaseConvertor;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import javax.swing.*;

import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonDebugEnv extends AbstractDebugEnv {
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

    public PythonDebugEnv(Project project) {
        super(project);
    }

    @Override
    public boolean prepare() throws DebugError {
        return buildToolPrepare() && testcasePrepare() && createSolutionFile() && createMainFile() && copyPyFile();
    }

    /**
     * copy python需要的代码
     * @return
     */
    private boolean copyPyFile() {
        // copy

        return
                copyFile("/debug/python/inst_source.py") &&
                copyFile("/debug/python/log_out_helper.py") &&
                copyFile("/debug/python/debug_core.py") &&
                copyFile("/debug/python/execute_result.py") &&
                copyFile("/debug/python/server.py");
    }

    private boolean copyFile(String resourcePath) {
        String[] split = resourcePath.split("/");
        String fileName = split[split.length - 1];
        return StoreService.getInstance(project).
                copyFile(getClass().getResource(resourcePath),
                        new FileUtils.PathBuilder(filePath).append(fileName).build()
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
        if (i == CANCEL_OPTION || i == NO_OPTION) {
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
        mainContent = mainContent.replace("{{callCode}}", getCallCode())
                .replace("{{port}}", String.valueOf(this.pyPort))
        ;
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
                "import sys\n";
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
}
