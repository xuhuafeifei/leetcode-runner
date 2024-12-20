package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static javax.swing.JOptionPane.CLOSED_OPTION;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeService {
    public static String DIR = "temp";

    /**
     * fill question and open code editor with preview content function
     *
     * @param question question
     * @param project project
     */
    public static void openCodeEditor(Question question, Project project) {
        QuestionService.getInstance().fillQuestion(question, project);

        // create code file
        String codeFilePath = createCodeFile(question);
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

    public static void reOpenCodeEditor(Question question, Project project, VirtualFile file, String langType) {
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

    private static LeetcodeEditor buildLeetcodeEditor(Question question, String translatedContent, String lang) {
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
        return le;
    }

    @Deprecated // not used
    private static String createContentFile(Question question) {
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

    private static String createCodeFile(Question question) {
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
    public static String getCodeFileName(Question question) {
        return question.getFileName() + AppSettings.getInstance().getFileTypeSuffix();
    }

    /**
     * 通过文件名称反解析question.fid
     * @param filePath 打开的虚拟文件的路径
     * @return frontedQuestionId
     */
    public static String parseFidFromFileName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseFrontendQuestionId(fileName);
    }

    /**
     * 通过vFile反解析question.fid
     * @param file idea打开的虚拟文件
     * @return frontedQuestionId
     */
    public static String parseFidFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseFidFromFileName(filePath);
    }

    /**
     * 通过文件名称反解析question.titleSlug
     * @param filePath 打开的虚拟文件的路径
     * @return 解析出titleSlug
     */
    public static String parseTitleSlugFromFileName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseTitleSlug(fileName);
    }

    /**
     * 通过vFile反解析question.titleSlug
     * @param file idea打开的虚拟文件
     * @return 解析titleSlug
     */
    public static String parseTitleSlugFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseTitleSlugFromFileName(filePath);
    }

    /**
     * 通过文件名反解析question的langType
     * @param filePath 打开的虚拟文件的路径
     */
    private static String parseLangType(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseLangType(fileName);
    }

    /**
     * 通过vFile反解析langType
     * @param file idea打开的虚拟文件
     * @return langType
     */
    public static String parseLangTypeFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseLangType(filePath);
    }

    private static RunCode buildRunCode(LeetcodeEditor lc, String codeContent) {
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
     * @param project project
     */
    public static void runCode(Project project) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(
                    "Some error happens, please close all file and try again!",
                    false,
                    true,
                    "Some error happens, please close all file and try again!",
                    "Run Code Error",
                    ConsoleDialog.ERROR
            );
            return;
        }
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Run code", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RunCodeResult rcr = LeetcodeClient.getInstance(project).runCode(runCode);
                // show it
                AbstractResultBuilder<RunCodeResult> rcrb = createRunCodeResultBuilder(runCode.getDataInput(), rcr);
                boolean correctAnswer = rcrb.isCorrectAnswer();
                if (correctAnswer) {
                    ConsoleUtils.getInstance(project).showInfo(rcrb.build(), true, true, "Congratulations!", "Run Code Result", ConsoleDialog.INFO);
                } else {
                    ConsoleUtils.getInstance(project).showInfo(rcrb.build(), true, true, "Oh No! Not Accept", "Run Code Result", ConsoleDialog.ERROR);
                }
            }
        });
    }

    public static void submitCode(Project project) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(
                    "Some error happens, please close all file and try again!",
                    false,
                    true,
                    "Some error happens, please close all file and try again!",
                    "Submit Code Error",
                    ConsoleDialog.ERROR
            );
            return;
        }

        // build run code
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Submit code", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                SubmitCodeResult scr = LeetcodeClient.getInstance(project).submitCode(runCode);
                // show it
                AbstractResultBuilder<SubmitCodeResult> scrb = createSubmitCodeResultBuilder(scr);
                boolean correctAnswer = scrb.isCorrectAnswer();
                if (correctAnswer) {
                    ConsoleUtils.getInstance(project).showInfo(scrb.build(), true, true, "Congratulations!", "Submit Code Result", ConsoleDialog.INFO);
                } else {
                    ConsoleUtils.getInstance(project).showInfo(scrb.build(), true, true, "Oh No! Not Accept!", "Submit Code Result", ConsoleDialog.ERROR);
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
     * @param project project
     */
    public static void rePosition(Project project) {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            JOptionPane.showMessageDialog(null, "No file is chosen");
            return;
        }
        // 获取当前打开文件的fid
        String fid = parseFidFromVFile(cFile);
        // 获取当前打开文件的titleSlug
        String titleSlug = parseTitleSlugFromVFile(cFile);
        // 获取当前打开文件的语言类型
        String langType = parseLangTypeFromVFile(cFile);
        if (fid == null || titleSlug == null) {
            JOptionPane.showMessageDialog(null, "Current file is not support to reposition");
            return;
        }
        if (! LangType.contains(langType)) {
            JOptionPane.showMessageDialog(null, "Current code type is not support. Your type = " + langType
                    + "\n"
                    + "Supported types: " + LangType.getAllLangType()
                    + "Please remove this file and choose question again!"
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
     * @param project project
     */
    public static void getDefaultContent(Project project) {
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
    private static abstract class AbstractResultBuilder<T extends BaseCodeResult> {
        protected T cr; // code result
        protected final StringBuilder sb = new StringBuilder();
        protected final String splitter = "--------------";
        private final String codeTypeSplitter = "===============";

        public AbstractResultBuilder (T cr) {
            this.cr = cr;
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
            sb.append("\n").append(codeTypeSplitter).append(" ").append("⚙ Setting Code Type : ").append(AppSettings.getInstance().getLangType()).append(" ").append(codeTypeSplitter).append("\n\n");
            if (correctAnswer) {
                // true
                sb.append("✅ Accept...").append("\n");
                sb.append("total test cases: ").append(cr.getTotalTestcases()).append("\n");
                sb.append("total correct: ").append(cr.getTotalCorrect()).append("\n");
            } else {
                boolean runSuccess = cr.getRunSuccess();
                if (runSuccess) {
                    // true
                    sb.append("❌ Wrong Answer...").append("\n");
                    sb.append("total test cases: ").append(cr.getTotalTestcases()).append("\n");
                    sb.append("total correct: ").append(cr.getTotalCorrect()).append("\n");
                }else {
                    // run error
                    if ("Runtime Error".equals(cr.getStatusMsg())) {
                        sb.append("❌ Runtime Error...").append("\n");
                        sb.append(cr.getFullRuntimeError()).append("\n");
                    }else if ("Compile Error".equals(cr.getStatusMsg())) {
                        sb.append("❌ Compile Error...").append("\n");
                        sb.append(cr.getFullCompileError()).append("\n");
                    }else if ("Time Limit Exceeded".equals(cr.getStatusMsg())) {
                        sb.append("❌ Time Limit Exceeded...").append("\n");
                        sb.append(cr.getFullCompileError()).append("\n");
                    }else {
                        throw new RuntimeException("unknown leetcode error...");
                    }
                }
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
    private static AbstractResultBuilder<RunCodeResult> createRunCodeResultBuilder(String dataInput, RunCodeResult cr) {
        return new AbstractResultBuilder<>(cr) {
            @Override
            protected void createBody() {
                String totalTestcases = cr.getTotalTestcases();
                if (StringUtils.isBlank(totalTestcases)) {
                    return;
                }
                int total = Integer.parseInt(totalTestcases);
                for (int i = 0; i < total; i++) {
                    sb.append(splitter).append("CASE ").append(i + 1).append(": ").append(cr.getCompareResult().charAt(i) == '1' ? "✅" : "❌").append(splitter).append("\n");
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

                sb.append("Expect Answer:").append("\n");
                sb.append(expectedCodeAnswer.get(i)).append("\n");
            }

            private void extractAnswer(int i) {
                List<String> codeAnswer = cr.getCodeAnswer();
                if (i >= codeAnswer.size()) return;

                sb.append("Code Answer:").append("\n");
                sb.append(codeAnswer.get(i)).append("\n");
            }

            private void extractStdoutput(int i) {
                List<String> stdOutputList = cr.getStdOutputList();
                if (i >= stdOutputList.size()) return;
                if (StringUtils.isBlank(stdOutputList.get(i))) return;

                sb.append("Standard Output:").append("\n");
                sb.append(stdOutputList.get(i)).append("\n");
            }

            private void extractInput(int i, int total) {
                if (StringUtils.isBlank(dataInput)) {
                    return;
                }
                String[] input = dataInput.split("\n");
                if (i >= input.length) return;

                sb.append("Input:").append("\n");
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
    private static AbstractResultBuilder<SubmitCodeResult> createSubmitCodeResultBuilder(SubmitCodeResult scr) {
        return new AbstractResultBuilder<>(scr) {
            @Override
            protected void createBody() {
                boolean correctAnswer = isCorrectAnswer();
                if (correctAnswer) {
                    return;
                }
                sb.append(splitter).append("LAST CASE").append(": ").append("❌").append(splitter).append("\n");
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

                sb.append("Expect Answer:").append("\n");
                sb.append(expectedOutput).append("\n");
            }

            private void extractAnswer() {
                String codeOutput = cr.getCodeOutput();
                if (StringUtils.isBlank(codeOutput)) return;

                sb.append("Code Answer:").append("\n");
                sb.append(codeOutput).append("\n");
            }

            private void extractStdoutput() {
                String stdOutput = cr.getStdOutput();
                if (StringUtils.isBlank(stdOutput)) return;

                sb.append("Standard Output:").append("\n");
                sb.append(stdOutput).append("\n");
            }

            private void extractInput() {
                String lastTestcase = cr.getLastTestcase();
                if (StringUtils.isBlank(lastTestcase)) return;

                String[] split = lastTestcase.split("\n");
                sb.append("Input:").append("\n");
                for (String s : split) {
                    sb.append(s).append("\n");
                }
            }
        };
    }


    /**
     * open test case dialog
     * @param project project
     */
    public static void openTestCasesDialog(Project project) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get example test cases
        String path = ViewUtils.getUnifyFilePathByVFile(Objects.requireNonNull(editor.getFile()));
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);

        if (lc == null) {
            ConsoleUtils.getInstance(project).showWaring(
                    "Some error happens, please close all file and try again!",
                    false,
                    true,
                    "Some error happens, please close all file and try again!",
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
