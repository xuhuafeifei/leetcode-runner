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
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.setting.AppSettings;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
        String markdownFilePath = createContentFile(question);

        LeetcodeEditor le = buildLeetcodeEditor(question, markdownFilePath);

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

    private static LeetcodeEditor buildLeetcodeEditor(Question question, String markdownFilePath) {
        LeetcodeEditor le = new LeetcodeEditor();
        le.setLang(AppSettings.getInstance().getLangType());
        le.setQuestionId(question.getQuestionId());
        le.setTitleSlug(question.getTitleSlug());
        le.setExampleTestcases(question.getExampleTestcases());
        le.setMarkdownPath(markdownFilePath);
        return le;
    }

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

    /**
     * run code with a teat case through a leetcode platform
     * @param project
     * @param codeContent
     */
    public static void runCode(Project project, RunCode codeContent) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Run Code", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RunCodeResult rcr = LeetcodeClient.getInstance(project).runCode(codeContent);
                // show it
                ConsoleUtils.getInstance(project).showInfo(
                        createRunCodeResultBuilder(codeContent.getDataInput(), rcr).build()
                );
            }
        });
    }

    public static void submitCode(Project project, RunCode codeContent) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Submit Code", false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                SubmitCodeResult scr = LeetcodeClient.getInstance(project).submitCode(codeContent);
                // show it
                ConsoleUtils.getInstance(project).showInfo(
                        createSubmitCodeResultBuilder(scr).build()
                );
            }
        });
    }


    /**
     * abstract result builder, used to build result string to show the run code result in console
     * <p>
     * RunCodeResult encapsulates the result obtained from running the submitted Solution code on the leetcode platform.
     * However, to ensure extensibility, an abstract class was extracted in the design to accommodate the output requirements of
     * SubmitCodeResult.
     *
     * @param <T>
     */
    private static abstract class AbstractResultBuilder<T extends BaseCodeResult> {
        protected T cr; // code result
        protected final StringBuilder sb = new StringBuilder();
        protected static final String splitter = "--------------";

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
                    }else {
                        throw new RuntimeException("unknown leetcode error...");
                    }
                }
            }
        }
        protected abstract void createBody();
    }

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
                    // extract input
                    extractInput(i, total);
                    // extract std_output
                    extractStdoutput(i, total);
                    // extract answer
                    extractAnswer(i, total);
                    // extract expected answer
                    extractExpectedAnswer(i, total);
                }
            }
            private void extractExpectedAnswer(int i, int total) {
                List<String> expectedCodeAnswer = cr.getExpectedCodeAnswer();
                if (expectedCodeAnswer.size() < total) return;

                sb.append("Expect Answer:").append("\n");
                sb.append(expectedCodeAnswer.get(i)).append("\n");
            }

            private void extractAnswer(int i, int total) {
                List<String> codeAnswer = cr.getCodeAnswer();
                if (codeAnswer.size() < total) return;

                sb.append("Code Answer:").append("\n");
                sb.append(codeAnswer.get(i)).append("\n");
            }

            private void extractStdoutput(int i, int total) {
                List<String> stdOutputList = cr.getStdOutputList();
                if (stdOutputList.size() < total) return;
                if (StringUtils.isBlank(stdOutputList.get(i))) return;

                sb.append("Standard Output:").append("\n");
                sb.append(stdOutputList.get(i)).append("\n");
            }

            private void extractInput(int i, int total) {
                if (StringUtils.isBlank(dataInput)) {
                    return;
                }
                String[] input = dataInput.split("\n");
                if (input.length < total) return;

                sb.append("Input:").append("\n");
                int size = input.length / total;
                int start = i * size, end = start + size;
                for (int k = start; k < end; ++k) {
                    sb.append(input[k]).append("\n");
                }
            }
        };
    }

    private static AbstractResultBuilder<SubmitCodeResult> createSubmitCodeResultBuilder(SubmitCodeResult scr) {
        return new AbstractResultBuilder<SubmitCodeResult>(scr) {
            @Override
            protected void createBody() {
                boolean correctAnswer = isCorrectAnswer();
                if (! correctAnswer) {
                    return;
                }
                sb.append(splitter).append("LAST CASE").append(": ").append("❌").append(splitter).append("\n");
                // extract input
                extractInput();
                // extract std_output
                extractStdoutput();
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
}
