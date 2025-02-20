package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.bus.CodeSubmitEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.RePositionEvent;
import com.xhf.leetcode.plugin.comp.TestCaseDialog;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.PythonCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.JOptionPane.CLOSED_OPTION;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeService {
    public static String DIR = "temp";

    // 单例
    private static volatile CodeService instance;
    private final Project project;

    public CodeService(Project project) {
        this.project = project;
    }

    // 单例
    public static CodeService getInstance(Project project) {
        if (instance == null) {
            synchronized (CodeService.class) {
                if (instance == null) {
                    instance = new CodeService(project);
                }
            }
        }
        return instance;
    }

    /**
     * fill question and open code editor with preview content function
     *
     * @param question question
     */
    public void openCodeEditor(Question question) {
        QuestionService.getInstance().fillQuestion(question, project);

        // create code file
        String codeFilePath = createCodeFile(question);
        analysisAndCreateFile(question);
        // create content file
        // String markdownFilePath = createContentFile(question);

        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent(), AppSettings.getInstance().getLangType());

        // store path info
        StoreService.getInstance(project).addCache(codeFilePath, le);
        // open code editor and load content
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(codeFilePath);
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    /**
     * deep coding 模式 创建代码
     * @param question question
     * @param deepCodingInfo dci
     */
    public void openCodeEditor(Question question, DeepCodingInfo deepCodingInfo) {
        QuestionService.getInstance().fillQuestion(question, project);

        // create code file
        String codeFilePath = createCodeFile(question);
        analysisAndCreateFile(question);
        // create content file
        // String markdownFilePath = createContentFile(question);

        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent(), AppSettings.getInstance().getLangType());
        le.setDeepCodingInfo(deepCodingInfo);

        // store path info
        StoreService.getInstance(project).addCache(codeFilePath, le);

        // open code editor and load content
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(codeFilePath);
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    /**
     * 分析代码, 同时创建文件
     * 主要服务于包含ListNode, TreeNode的题目
     * @param question question
     */
    private void analysisAndCreateFile(Question question) {
        String codeSnippets = question.getCodeSnippets();
        if (StringUtils.isBlank(codeSnippets)) {
            return;
        }
        // 分析代码
        LangType langType = LangType.getType(AppSettings.getInstance().getLangType());
        if (langType == null) {
            LogUtils.warn("langType is null !!!");
            return;
        }
        switch (langType) {
            case JAVA:
                AnalysisResult a1 = new JavaCodeAnalyzer(project).analyze(codeSnippets);
                if (contains(a1, "ListNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/java/ListNode.java")), "ListNode.java");
                }
                if (contains(a1, "TreeNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/java/TreeNode.java")), "TreeNode.java");
                }
                return;
            case PYTHON3:
                AnalysisResult a2 = new PythonCodeAnalyzer(project).analyze(codeSnippets);
                if (contains(a2, "ListNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/python/ListNode.py")), "ListNode.py");
                }
                if (contains(a2, "TreeNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/python/TreeNode.py")), "TreeNode.py");
                }
                return;
            default:
                LogUtils.warn("当前语言并不支持自动创建ListNode或TreeNode, langType = " + langType.getLangType());
        }
    }

    private boolean contains(AnalysisResult ar, String paramTypeName) {
        for (String parameterType : ar.getParameterTypes()) {
            if (parameterType.contains(paramTypeName)) {
                return true;
            }
        }
        return false;
    }

    public void reOpenCodeEditor(Question question, VirtualFile file, String langType) {
        QuestionService.getInstance().fillQuestion(question, project);

        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent(), langType);
        // get current file path
        String currentFilePath = ViewUtils.getUnifyFilePathByVFile(file);
        // restore
        StoreService.getInstance(project).addCache(currentFilePath, le);
        // close
        ViewUtils.closeVFile(file, project);
        // reopen
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile reFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(currentFilePath);
            if (reFile != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    public void reOpenCodeEditor(Question question, VirtualFile file, String langType, DeepCodingInfo deepCodingInfo) {
        QuestionService.getInstance().fillQuestion(question, project);

        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent(), langType);
        le.setDeepCodingInfo(deepCodingInfo);

        // get current file path
        String currentFilePath = ViewUtils.getUnifyFilePathByVFile(file);
        // restore
        StoreService.getInstance(project).addCache(currentFilePath, le);
        // close
        ViewUtils.closeVFile(file, project);
        // reopen
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile reFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(currentFilePath);
            if (reFile != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    private LeetcodeEditor buildLeetcodeEditor(Question question, String translatedContent, String lang) {
        LeetcodeEditor le = new LeetcodeEditor();
        le.setLang(lang);
        le.setQuestionId(question.getQuestionId());
        le.setTitleSlug(question.getTitleSlug());
        le.setExampleTestcases(question.getExampleTestcases());
        le.setDefaultTestcases(question.getExampleTestcases());
        le.setFrontendQuestionId(question.getFrontendQuestionId());
        // le.setMarkdownPath(markdownFilePath);
        le.setMarkdownContent(translatedContent);
        le.setTranslatedTitle(question.getTranslatedTitle());
        le.setDifficulty(question.getDifficulty());
        le.setDebugTestcase(question.getExampleTestcases());
        le.setStatus(question.getStatus());
        return le;
    }

    @Deprecated // not used
    private String createContentFile(Question question) {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
                .append("content")
                .append(question.getFileName() + ".md")
                .build();

        try {
            if (FileUtils.fileExists(filePath)) {
                return filePath;
            }
            FileUtils.createAndWriteFile(filePath, question.getTranslatedContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filePath;
    }

    /**
     * 根据文件名称 + 文件内容, 创建文件
     *
     * @param content
     * @param fileName
     * @return
     */
    private String createCodeFile(String content, String fileName) {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
                // .append("temp")
                .append(fileName)
                .build();

        try {
            FileUtils.createAndWriteFile(filePath, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 刷新文件系统
        LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
        return filePath;
    }

    private String createCodeFile(Question question) {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
                // .append("temp")
                .append(getCodeFileName(question))
                .build();

        try {
            if (FileUtils.fileExists(filePath)) {
                return filePath;
            }
            FileUtils.createAndWriteFile(filePath, question.getCodeSnippets());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filePath;
    }

    /**
     * 通过question创建文件名
     * @param question question
     * @return 获取文件名
     */
    public String getCodeFileName(Question question) {
        return question.getFileName() + AppSettings.getInstance().getFileTypeSuffix();
    }

    /**
     * 通过文件名称反解析question.fid
     * @param filePath 打开的虚拟文件的路径
     * @return frontedQuestionId
     */
    public String parseFidFromFileName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseFrontendQuestionId(fileName);
    }

    /**
     * 通过vFile反解析question.fid
     * @param file idea打开的虚拟文件
     * @return frontedQuestionId
     */
    public String parseFidFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseFidFromFileName(filePath);
    }

    /**
     * 通过文件名称反解析question.titleSlug
     * @param filePath 打开的虚拟文件的路径
     * @return 解析出titleSlug
     */
    public String parseTitleSlugFromFileName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseTitleSlug(fileName);
    }

    /**
     * 通过vFile反解析question.titleSlug
     * @param file idea打开的虚拟文件
     * @return 解析titleSlug
     */
    public String parseTitleSlugFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseTitleSlugFromFileName(filePath);
    }

    /**
     * 通过文件名反解析question的langType
     * @param filePath 打开的虚拟文件的路径
     */
    private String parseLangType(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseLangType(fileName);
    }

    /**
     * 通过vFile反解析langType
     * @param file idea打开的虚拟文件
     * @return langType
     */
    public String parseLangTypeFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseLangType(filePath);
    }

    /**
     * 通过当前打开的vFile 反解析langType
     * @param project project
     * @return langType
     */
    public String parseLangTypeFromCVFile(Project project) {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            JOptionPane.showMessageDialog(null, "No file is chosen");
            return null;
        }
        return parseLangTypeFromVFile(cFile);
    }

    private RunCode buildRunCode(LeetcodeEditor lc, String codeContent) {
        // build run code
        RunCode runCode = new RunCode();
        runCode.setFrontendQuestionId(lc.getFrontendQuestionId());
        runCode.setQuestionId(lc.getQuestionId());
        // runCode.setLang(lc.getLang());
        /*
            一切提交代码类型按照setting中的设置确定
         */
        String langType = AppSettings.getInstance().getLangType();
        runCode.setLang(langType);
        // 处理codeContent, 截取代码片段
        String coreCode = Question.getCoreCodeSnippets(codeContent, langType);
        /*
            这里额外做一层保障, 如果用户随意删改lineStart和endStart, 那么最终得到的coreCode可能会出现问题
            如果截取为空, 则不做任何截取操作
         */
        runCode.setTypeCode(StringUtils.isBlank(coreCode) ? codeContent : coreCode);
        runCode.setTitleSlug(lc.getTitleSlug());
        runCode.setDataInput(lc.getExampleTestcases());
        return runCode;
    }

    /**
     * run code with a teat case through a leetcode platform
     */
    public void runCode() {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(
                    "发生了一些错误, 请重新打开文件或对文件进行重定位",
                    false,
                    true,
                    "发生了一些错误, 请重新打开文件或对文件进行重定位",
                    "运行代码 异常",
                    ConsoleDialog.ERROR
            );
            return;
        }
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "运行代码", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RunCodeResult rcr = LeetcodeClient.getInstance(project).runCode(runCode);
                // show it
                AbstractResultBuilder<RunCodeResult> rcrb = createRunCodeResultBuilder(runCode.getDataInput(), rcr, project);
                boolean correctAnswer = rcrb.isCorrectAnswer();
                if (correctAnswer) {
                    ConsoleUtils.getInstance(project).showInfo(rcrb.build(), true, true, "运行通过!", "运行代码 结果", ConsoleDialog.INFO);
                } else {
                    ConsoleUtils.getInstance(project).showInfo(rcrb.build(), true, true, "Oh No! 运行不通过!", "运行代码 结果", ConsoleDialog.ERROR);
                }
            }
        });
    }

    public void submitCode() {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(
                    "发生了一些错误, 请重新打开文件或对文件进行重定位",
                    false,
                    true,
                    "发生了一些错误, 请重新打开文件或对文件进行重定位",
                    "提交代码 异常",
                    ConsoleDialog.ERROR
            );
            return;
        }

        // build run code
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "提交代码", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                SubmitCodeResult scr = LeetcodeClient.getInstance(project).submitCode(runCode);
                // show it
                AbstractResultBuilder<SubmitCodeResult> scrb = createSubmitCodeResultBuilder(scr, project);
                boolean correctAnswer = scrb.isCorrectAnswer();
                if (correctAnswer) {
                    ConsoleUtils.getInstance(project).showInfo("运行成功", true, true, "运行通过!", "运行代码 结果", ConsoleDialog.INFO);
                } else {
                    ConsoleUtils.getInstance(project).showError("运行失败", true, true, "Oh No! 运行不通过!", "运行代码 结果", ConsoleDialog.ERROR);
                }

                ConsoleUtils.getInstance(project).showInfo(scrb.build());
                // update question
                boolean update = QuestionService.getInstance().updateQuestionStatusByFqid(project, runCode.getFrontendQuestionId(), scrb.isCorrectAnswer());
                // post
                if (update) {
                    LCEventBus.getInstance().post(new CodeSubmitEvent(project));
                }
            }
        });
    }

    /**
     * 重新定位question
     */
    public void rePosition() {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            JOptionPane.showMessageDialog(null, "没有选择问题文件");
            return;
        }
        // 获取当前打开文件的fid
        String fid = parseFidFromVFile(cFile);
        // 获取当前打开文件的titleSlug
        String titleSlug = parseTitleSlugFromVFile(cFile);
        // 获取当前打开文件的语言类型
        String langType = parseLangTypeFromVFile(cFile);
        if (fid == null || titleSlug == null) {
            JOptionPane.showMessageDialog(null, "当前文件不支持重定位");
            return;
        }
        if (! LangType.contains(langType)) {
            JOptionPane.showMessageDialog(null, "当前文件类型不支持. 你的文件类型是 = " + langType
                    + "\n"
                    + "支持的文件类型是 : " + LangType.getAllLangType() + "\n"
                    + "请移除当前文件并重新选择问题"
            );
            return;
        }
        // 遍历myList
        LCEventBus.getInstance().post(new RePositionEvent(fid, titleSlug, cFile, langType));
    }

    /**
     * 获取默认代码, 并写入当前打开文件
     * <p>
     * 该方法会通过当前打开的文件解析出fid和titleSlug, 并且根据这两个值获取默认代码
     *
     */
    public void getDefaultContent() {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            JOptionPane.showMessageDialog(null, "No file open");
            return;
        }
        String titleSlug = parseTitleSlugFromVFile(cFile);
        String langType = parseLangTypeFromVFile(cFile);
        // 插件设置时, 设定的langType
        String settingLangType = AppSettings.getInstance().getLangType();

        // 文件所代表的code类型
        if (! LangType.contains(langType)) {
            JOptionPane.showMessageDialog(null, "current code file type can not be identified." +
                    "\r\n" +
                    "plugin will load content type from your plugin setting.\r\nyour code file type = " + langType +
                    "Setting LangType = " + settingLangType
            );
        }
        else if (! LangType.equals(langType, settingLangType)) {
            int result = JOptionPane.showOptionDialog(
                    null,
                    "Are you sure to load content from setting?" + "\r\n" +
                            "Your code file type = " + langType + "\r\n" +
                            "Setting LangType = " + settingLangType + "\r\n"
                    ,
                    "Load Default Code",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Yes", "No"},
                    "Yes"
            );
            if (result == CLOSED_OPTION) {
                return;
            }
        }
        // 通过titleSlug查询question的content
        Question question = QuestionService.getInstance().queryQuestionInfo(titleSlug, project);

        /*
            查询默认代码, 并写入当前打开的文件
         */
        if (question != null && StringUtils.isNotBlank(question.getCodeSnippets())) {
            String defaultCode = question.getCodeSnippets();

            boolean flag = ViewUtils.writeContentToVFile(cFile, defaultCode);
            if (flag) {
                JOptionPane.showMessageDialog(null, "load default content success!");
            }else {
                JOptionPane.showMessageDialog(null, "load default content error!");
            }

        } else {
            JOptionPane.showMessageDialog(null, "Question not found for title slug: " + titleSlug);
        }
    }


    /**
     * 抽象结果构建器，用于构建结果字符串以在控制台显示RunCode/SubmitCode的结果
     * <p>
     * RunCodeResult 封装了在 leetcode 平台上运行提交的解决方案代码所获得的结果。
     * <p>
     * 然而，为了确保可扩展性，在设计中提取了一个抽象类，以适应 SubmitCodeResult 和 RunCodeResult 的输出需求。
     *
     * @param <T>
     */
    private abstract static class AbstractResultBuilder<T extends BaseCodeResult> {
        private final Project project;
        protected T cr; // code result
        protected final StringBuilder sb = new StringBuilder();
        protected final String splitter = "--------------";
        private final String codeTypeSplitter = "===============";
        private final Pattern pattern = Pattern.compile("Line (\\d+):");

        public AbstractResultBuilder (T cr, Project project) {
            this.cr = cr;
            this.project = project;
        }

        public String build() {
            createHead();
            createBody();
            return sb.toString();
        }

        protected boolean isCorrectAnswer() {
            return Objects.equals(cr.getTotalCorrect(), cr.getTotalTestcases()) &&
                    StringUtils.isNotBlank(cr.getTotalCorrect()) &&
                    StringUtils.isNotBlank(cr.getTotalTestcases());
        }

        /*
         * 创建头部:
         * ========================== example 1 ============================
         *  ✅ Accept...
         *  total test cases: 3
         *  total correct: 3
         *
         * ========================== example 2 ============================
         *  ❌ Wrong Answer...
         *  total test cases: 3
         *  total correct: 0
         *
         * ========================== example 3 ============================
         * ❌ Runtime Error...
         *  NameError: name 'abab' is not defined
         *      abab
         *  Line 5 in semiOrderedPermutation (Solution.py)
         *            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         *      ret = Solution().semiOrderedPermutation(param_1)
         *  Line 31 in _driver (Solution.py)
         *      _driver()
         *  Line 46 in <module> (Solution.py)
         */
        private void createHead() {
            boolean correctAnswer = isCorrectAnswer();
            sb.append("\n").append(codeTypeSplitter).append(" ").append("⚙ 设置的代码类型 : ").append(AppSettings.getInstance().getLangType()).append(" ").append(codeTypeSplitter).append("\n\n");
            if (correctAnswer) {
                // true
                sb.append("✅ 通过...").append("\n");
                sb.append("⏰: ").append(cr.getDisplayRuntime()).append(" s ").append(" 💽: ").append(cr.getStatusMemory()).append("\n");
                sb.append("全部的测试案例数量: ").append(cr.getTotalTestcases()).append("\n");
                sb.append("通过的测试案例数量: ").append(cr.getTotalCorrect()).append("\n");
            } else {
                boolean runSuccess = cr.getRunSuccess();
                if (runSuccess) {
                    // true
                    sb.append("❌ 答案错误 ...").append("\n");
                    sb.append("全部的测试案例数量: ").append(cr.getTotalTestcases()).append("\n");
                    sb.append("通过的测试案例数量: ").append(cr.getTotalCorrect()).append("\n");
                }else {
                    // run error
                    if ("Runtime Error".equals(cr.getStatusMsg())) {
                        sb.append("❌ Runtime Error...").append("\n");
                        sb.append(DebugUtils.matchLines(cr.getFullRuntimeError(), Question.getLineUpperOffset(project))).append("\n");
                    }else if ("Compile Error".equals(cr.getStatusMsg())) {
                        sb.append("❌ Compile Error...").append("\n");
                        sb.append(DebugUtils.matchLines(cr.getFullCompileError(), Question.getLineUpperOffset(project))).append("\n");
                    }else if ("Time Limit Exceeded".equals(cr.getStatusMsg())) {
                        sb.append("❌ Time Limit Exceeded...").append("\n");
                        sb.append(DebugUtils.matchLines(cr.getFullCompileError(), Question.getLineUpperOffset(project))).append("\n");
                    }else {
                        // throw new RuntimeException("unknown leetcode error...");
                        sb.append("❌ " + cr.getStatusMsg()).append("\n");
                    }
                }
            }
        }

        /**
         * 处理错误行信息: 比如
         * ❌ Compile Error...
         * Line 36: error: not a statement
         *         a
         *         ^
         * 我要提取 Line 36, 然后对36进行行号校正. 因为存在注释偏移
         *
         * @param error
         * @return
         */
        private String handleErrorInfo(String error) {
            // 正则表达式提取行号
            Matcher matcher = pattern.matcher(error);

            if (matcher.find()) {
                // 提取行号
                String lineNumberStr = matcher.group(1);
                int lineNumber = Integer.parseInt(lineNumberStr);
                // 对行号进行操作
                int newLineNumber = Question.getLineUpperOffset(project);  // 示例：将行号加10
                return error.replaceFirst("Line " + lineNumberStr + ":", "Line " + newLineNumber + ":");
            } else {
                return error;
            }
        }

        /**
         * 创建body
         * ========================== example 1 ============================
         * --------------CASE 1: ✅--------------
         * Input:
         * [2,1,4,3]
         * Code Answer:
         * 2
         * Expect Answer:
         * 2
         * --------------CASE 2: ✅--------------
         * Input:
         * [2,4,1,3]
         * Code Answer:
         * 3
         * Expect Answer:
         * 3
         * --------------CASE 3: ✅--------------
         * Input:
         * [1,3,4,2,5]
         * Code Answer:
         * 0
         * Expect Answer:
         * 0
         * <p>
         * ========================== example 2 ============================
         * --------------CASE 1: ❌--------------
         * Input:
         * [2,1,4,3]
         * Code Answer:
         * 4
         * Expect Answer:
         * 2
         * --------------CASE 2: ❌--------------
         * Input:
         * [2,4,1,3]
         * Code Answer:
         * 5
         * Expect Answer:
         * 3
         * --------------CASE 3: ❌--------------
         * Input:
         * [1,3,4,2,5]
         * Code Answer:
         * 2
         * Expect Answer:
         * 0
         * <p>
         * ========================== example 3 ============================
         * null
         */
        protected abstract void createBody();
    }

    /**
     * 创建负责输出RunCode结果的类
     *
     * @param dataInput 输入测试案例
     * @param cr RunCode结果, 来自于leetcode platform
     * @return 返回builder, 用于build创建string, 输出到LCConsole
     */
    private AbstractResultBuilder<RunCodeResult> createRunCodeResultBuilder(String dataInput, RunCodeResult cr, Project project) {
        return new AbstractResultBuilder<>(cr, project) {
            @Override
            protected void createBody() {
                String totalTestcases = cr.getTotalTestcases();
                if (StringUtils.isBlank(totalTestcases)) {
                    return;
                }
                int total = Integer.parseInt(totalTestcases);
                for (int i = 0; i < total; i++) {
                    sb.append(splitter).append("测试案例 ").append(i + 1).append(": ").append(cr.getCompareResult().charAt(i) == '1' ? "✅" : "❌").append(splitter).append("\n");
                    // extract std_output
                    extractStdoutput(i);
                    // extract input
                    extractInput(i, total);
                    // extract answer
                    extractAnswer(i);
                    // extract expected answer
                    extractExpectedAnswer(i);
                }
            }

            private void extractExpectedAnswer(int i) {
                List<String> expectedCodeAnswer = cr.getExpectedCodeAnswer();
                if (i >= expectedCodeAnswer.size()) return;

                sb.append("期待的答案:").append("\n");
                sb.append(expectedCodeAnswer.get(i)).append("\n");
            }

            private void extractAnswer(int i) {
                List<String> codeAnswer = cr.getCodeAnswer();
                if (i >= codeAnswer.size()) return;

                sb.append("运行结果:").append("\n");
                sb.append(codeAnswer.get(i)).append("\n");
            }

            private void extractStdoutput(int i) {
                List<String> stdOutputList = cr.getStdOutputList();
                if (i >= stdOutputList.size()) return;
                if (StringUtils.isBlank(stdOutputList.get(i))) return;

                sb.append("标准输出:").append("\n");
                sb.append(stdOutputList.get(i)).append("\n");
            }

            private void extractInput(int i, int total) {
                if (StringUtils.isBlank(dataInput)) {
                    return;
                }
                String[] input = dataInput.split("\n");
                if (i >= input.length) return;

                sb.append("输入:").append("\n");
                int size = input.length / total;
                int start = i * size, end = start + size;
                for (int k = start; k < end; ++k) {
                    sb.append(input[k]).append("\n");
                }
            }
        };
    }

    /**
     * 创建负责输出SubmitCode结果的类
     *
     * @param scr SubmitCodeResult, 来自于leetcode platform
     * @return 返回builder, 用于build创建string, 输出到LCConsole
     */
    private AbstractResultBuilder<SubmitCodeResult> createSubmitCodeResultBuilder(SubmitCodeResult scr, Project project) {
        return new AbstractResultBuilder<>(scr, project) {
            @Override
            protected void createBody() {
                boolean correctAnswer = isCorrectAnswer();
                if (correctAnswer) {
                    return;
                }
                sb.append(splitter).append("上一个测试案例").append(": ").append("❌").append(splitter).append("\n");
                // extract std_output
                extractStdoutput();
                // extract input
                extractInput();
                // extract answer
                extractAnswer();
                // extract expected answer
                extractExpectedAnswer();
            }

            private void extractExpectedAnswer() {
                String expectedOutput = cr.getExpectedOutput();
                if (StringUtils.isBlank(expectedOutput)) return;

                sb.append("期待的答案:").append("\n");
                sb.append(expectedOutput).append("\n");
            }

            private void extractAnswer() {
                String codeOutput = cr.getCodeOutput();
                if (StringUtils.isBlank(codeOutput)) return;

                sb.append("运行结果:").append("\n");
                sb.append(codeOutput).append("\n");
            }

            private void extractStdoutput() {
                String stdOutput = cr.getStdOutput();
                if (StringUtils.isBlank(stdOutput)) return;

                sb.append("标准输出:").append("\n");
                sb.append(stdOutput).append("\n");
            }

            private void extractInput() {
                String lastTestcase = cr.getLastTestcase();
                if (StringUtils.isBlank(lastTestcase)) return;

                String[] split = lastTestcase.split("\n");
                sb.append("输入:").append("\n");
                for (String s : split) {
                    sb.append(s).append("\n");
                }
            }
        };
    }


    /**
     * open test case dialog
     */
    public void openTestCasesDialog() {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get example test cases
        String path = ViewUtils.getUnifyFilePathByVFile(Objects.requireNonNull(editor.getFile()));
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);

        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(
                    "发生了一些错误, 请重新打开文件或对文件进行重定位",
                    false,
                    true,
                    "发生了一些错误, 请重新打开文件或对文件进行重定位",
                    "Test Cases Set Error",
                    ConsoleDialog.ERROR
            );
            return;
        }

        // check testcase data input
        if (lc.getExampleTestcases() == null || lc.getDefaultTestcases() == null) {
            // load example
            Question q = QuestionService.getInstance().queryQuestionInfo(lc.getTitleSlug(), project);
            if (StringUtils.isBlank(q.getExampleTestcases())) {
                throw new RuntimeException("No example test cases found...");
            }
            lc.setExampleTestcases(q.getExampleTestcases());
            lc.setDefaultTestcases(q.getExampleTestcases());
            // restore
            StoreService.getInstance(project).addCache(path, lc);
        }

        // create dialog
        new TestCaseDialog(
                lc.getExampleTestcases(), path, project
        ).show();
    }
}
