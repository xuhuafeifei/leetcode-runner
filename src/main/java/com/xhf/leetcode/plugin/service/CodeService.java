package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
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
                    }else if ("Time Limit Exceeded".equals(cr.getStatusMsg())) {
                        sb.append("❌ Time Limit Exceeded...").append("\n");
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
     * @param lc
     * @param path
     * @param project
     */
    public static void openTestCasesDialog(LeetcodeEditor lc, String path, Project project) {
        // create dialog
        new TestCasesDialog(
                lc.getExampleTestcases(), path, project
        ).show();
    }

    static class TestCasesDialog extends DialogWrapper {
        private String dataInput;
        private JPanel contentPane;
        private JTextArea textArea;
        // java file path, which is used for a key in cache
        private String path;
        private Project project;
        private JButton resetButton;

        public TestCasesDialog(String dataInput, String path, Project project) {
            super(true);
            this.dataInput = dataInput;
            this.path = path;
            this.project = project;
            init();
            setTitle("Set Test Cases");
            setSize(600, 400);
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            contentPane = new JPanel(new BorderLayout());
            textArea = new JTextArea(400, 400);
            textArea.setText(this.dataInput);
            textArea.setEditable(true);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPane.add(scrollPane, BorderLayout.CENTER);

            return contentPane;
        }

        @Override
        protected @Nullable JComponent createSouthPanel() {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            // create reset button
            resetButton = new JButton("Reset");
            resetButton.setBorderPainted(false);
            resetButton.setContentAreaFilled(false);
            resetButton.setFocusPainted(false);
            resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            resetButton.addActionListener(e -> {
                // get default data
                LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
                String defaultTestcases = lc.getDefaultTestcases();
                textArea.setText(defaultTestcases);
            });
            buttonPanel.add(resetButton);


            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> doOKAction());
            buttonPanel.add(okButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> doCancelAction());
            buttonPanel.add(cancelButton);

            return buttonPanel;
        }

        @Override
        protected void doOKAction() {
            String inputText = textArea.getText();
            inputText = inputText.trim();
            // update data
            LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);
            lc.setExampleTestcases(inputText);
            StoreService.getInstance(project).addCache(path, lc);
            super.doOKAction();
        }
    }
}
