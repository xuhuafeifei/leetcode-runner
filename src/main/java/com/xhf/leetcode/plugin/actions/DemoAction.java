package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DemoAction extends AnAction {
    private Project project;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        project = e.getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        VirtualFile file2 = LocalFileSystem.getInstance().refreshAndFindFileByPath("E:\\java_code\\leetcode-runner\\src\\main\\java\\com\\xhf\\leetcode\\plugin\\window\\LCToolWindowFactory.java");

        open(file2, fileEditorManager);

//        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath("E:\\java_code\\leetcode-runner\\src\\main\\resources\\META-INF\\plugin.xml");
//
//        open(file, fileEditorManager);
    }

    private void open(VirtualFile file, FileEditorManager fileEditorManager) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                fileEditorManager.openTextEditor(ofd, false);
            }
        });

    }
}
