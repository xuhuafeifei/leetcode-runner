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
import com.xhf.leetcode.plugin.comp.TestCaseDialog;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeService {
    public static String DIR = "temp";

    /**
     * fill question and open code editor with preview content function
     *
     * @param question
     * @param project
     */
    public static void openCodeEditor(Question question, Project project) {
        QuestionService.getInstance().fillQuestion(question, project);

        // create code file
        String codeFilePath = createCodeFile(question);
        // create content file
        // String markdownFilePath = createContentFile(question);

        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent());

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

    private static LeetcodeEditor buildLeetcodeEditor(Question question, String translatedContent) {
        LeetcodeEditor le = new LeetcodeEditor();
        le.setLang(AppSettings.getInstance().getLangType());
        le.setQuestionId(question.getQuestionId());
        le.setTitleSlug(question.getTitleSlug());
        le.setExampleTestcases(question.getExampleTestcases());
        le.setDefaultTestcases(question.getExampleTestcases());
        le.setFrontendQuestionId(question.getFrontendQuestionId());
        // le.setMarkdownPath(markdownFilePath);
        le.setMarkdownContent(translatedContent);
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
                .append(question.getFileName() + AppSettings.getInstance().getFileTypeSuffix())
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

    private static RunCode buildRunCode(LeetcodeEditor lc, String codeContent) {
        // build run code
        RunCode runCode = new RunCode();
        runCode.setFrontendQuestionId(lc.getFrontendQuestionId());
        runCode.setQuestionId(lc.getQuestionId());
        runCode.setLang(lc.getLang());
        runCode.setTypeCode(codeContent);
        runCode.setTitleSlug(lc.getTitleSlug());
        runCode.setDataInput(lc.getExampleTestcases());
        return runCode;
    }

    /**
     * run code with a teat case through a leetcode platform
     * @param project
     */
    public static void runCode(Project project) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        assert lc != null;
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Run Code", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RunCodeResult rcr = LeetcodeClient.getInstance(project).runCode(runCode);
                // show it
                AbstractResultBuilder<RunCodeResult> rcrb = createRunCodeResultBuilder(runCode.getDataInput(), rcr);
                ConsoleUtils.getInstance(project).showInfo(rcrb.build());
            }
        });
    }

    public static void submitCode(Project project) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);
        assert lc != null;

        // build run code
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Submit Code", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                SubmitCodeResult scr = LeetcodeClient.getInstance(project).submitCode(runCode);
                // show it
                AbstractResultBuilder<SubmitCodeResult> scrb = createSubmitCodeResultBuilder(scr);
                ConsoleUtils.getInstance(project).showInfo(scrb.build());
                // update question
                QuestionService.getInstance().updateQuestionStatusByFqid(project, runCode.getFrontendQuestionId(), scrb.isCorrectAnswer());
                // post
                LCEventBus.getInstance().post(new CodeSubmitEvent(project));
            }
        });
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
         *
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
        return new AbstractResultBuilder<RunCodeResult>(cr) {
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
                    extractStdoutput(i, total);
                    // extract input
                    extractInput(i, total);
                    // extract answer
                    extractAnswer(i, total);
                    // extract expected answer
                    extractExpectedAnswer(i, total);
                }
            }
            private void extractExpectedAnswer(int i, int total) {
                List<String> expectedCodeAnswer = cr.getExpectedCodeAnswer();
                if (i >= expectedCodeAnswer.size()) return;

                sb.append("Expect Answer:").append("\n");
                sb.append(expectedCodeAnswer.get(i)).append("\n");
            }

            private void extractAnswer(int i, int total) {
                List<String> codeAnswer = cr.getCodeAnswer();
                if (i >= codeAnswer.size()) return;

                sb.append("Code Answer:").append("\n");
                sb.append(codeAnswer.get(i)).append("\n");
            }

            private void extractStdoutput(int i, int total) {
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
        return new AbstractResultBuilder<SubmitCodeResult>(scr) {
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
     * @param project
     */
    public static void openTestCasesDialog(Project project) {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get example test cases
        String path = editor.getFile().getPath();
        path = FileUtils.unifyPath(path);
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
        assert lc != null;

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
