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
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.model.RunCode;
import com.xhf.leetcode.plugin.model.RunCodeResult;
import com.xhf.leetcode.plugin.setting.AppSettings;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

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
                RunCodeResult res = LeetcodeClient.getInstance(project).runCode(codeContent);
                // show it
                ConsoleUtils.getInstance(project).showInfo(
                        new ResultBuilder(res, codeContent.getDataInput()).build()
                );
            }
        });

    }

    private static class ResultBuilder {
        private String dataInput;
        private StringBuilder sb;
        private RunCodeResult rcr;
        public ResultBuilder(RunCodeResult rcr, String dataInput) {
            this.sb = new StringBuilder();
            this.rcr = rcr;
            this.dataInput = dataInput;
        }

        public String build() {
            createHead();
            createBody();
            return sb.toString();
        }

        private void createHead() {
            boolean correctAnswer = rcr.getCorrectAnswer();
            if (correctAnswer) {
                // true
                sb.append("Accept...").append("\n");
                sb.append("total test cases: ").append(rcr.getTotalTestcases()).append("\n");
                sb.append("total correct: ").append(rcr.getTotalCorrect()).append("\n");
            } else {
                boolean runSuccess = rcr.getRunSuccess();
                if (runSuccess) {
                    // true
                    sb.append("Answer Wrong ...").append("\n");
                    sb.append("total test cases: ").append(rcr.getTotalTestcases()).append("\n");
                    sb.append("total correct: ").append(rcr.getTotalCorrect()).append("\n");
                }else {
                    // run error
                    if ("Runtime Error".equals(rcr.getStatusMsg())) {
                        sb.append("Runtime Error...").append("\n");
                        sb.append(rcr.getFullRuntimeError()).append("\n");
                    }else if ("Compile Error".equals(rcr.getStatusMsg())) {
                        sb.append("Compile Error...").append("\n");
                        sb.append(rcr.getFullCompileError()).append("\n");
                    }else {
                        throw new RuntimeException("unknown leetcode error...");
                    }
                }
            }
        }

        private static final String spliter = "--------------";

        private void createBody() {
            String totalTestcases = rcr.getTotalTestcases();
            if (StringUtils.isBlank(totalTestcases)) {
                return;
            }
            int total = Integer.parseInt(totalTestcases);
            for (int i = 0; i < total; i++) {
                sb.append(spliter).append("CASE ").append(i + 1).append(": ").append(rcr.getCompareResult().charAt(i) == '1' ? "✅" : "❌").append(spliter).append("\n");
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
            List<String> expectedCodeAnswer = rcr.getExpectedCodeAnswer();
            if (expectedCodeAnswer.size() < total) return;

            sb.append("Expect Answer:").append("\n");
            sb.append(expectedCodeAnswer.get(i)).append("\n");
        }

        private void extractAnswer(int i, int total) {
            List<String> codeAnswer = rcr.getCodeAnswer();
            if (codeAnswer.size() < total) return;

            sb.append("Code Answer:").append("\n");
            sb.append(codeAnswer.get(i)).append("\n");
        }

        private void extractStdoutput(int i, int total) {
            List<String> stdOutputList = rcr.getStdOutputList();
            if (stdOutputList.size() < total) return;
            if (StringUtils.isBlank(stdOutputList.get(i))) return;

            sb.append("Standard Output:").append("\n");
            sb.append(stdOutputList.get(i)).append("\n");
        }

        private void extractInput(int i, int total) {
            String[] input = dataInput.split("\n");
            if (input.length < total) return;

            sb.append("Input:").append("\n");
            int size = input.length / total;
            int start = i * size, end = start + size;
            for (int k = start; k < end; ++k) {
                sb.append(input[k]).append("\n");
            }
        }
    }
}
