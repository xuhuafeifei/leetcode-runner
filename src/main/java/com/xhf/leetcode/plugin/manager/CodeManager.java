package com.xhf.leetcode.plugin.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.setting.AppSettings;

import java.io.IOException;

public class CodeManager {
    public static String DIR = "temp";

    /**
     * fill question and open code editor with preview content function
     *
     * @param question
     * @param project
     */
    public static void openCodeEditor(Question question, Project project) {
        LeetcodeClient lcClient = LeetcodeClient.getInstance(project);
        lcClient.fillQuestion(question);

        // create code file
        String key = createCodeFile(question);
        // create content file
        String value = createContentFile(question);
        // store path info
        StoreService.getInstance(project).addCache(key, value);
        // open code editor and load content
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(key);
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });

    }

    private static String createContentFile(Question question) {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
                .append("content")
                .append(question.getFileName() + ".md")
                .build();

        try {
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
            FileUtils.createAndWriteFile(filePath, question.getCodeSnippets());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filePath;
    }
}
