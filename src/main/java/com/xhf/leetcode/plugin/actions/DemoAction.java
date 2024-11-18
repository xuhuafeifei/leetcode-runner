package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DemoAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e)  {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        VirtualFile file2 = LocalFileSystem.getInstance().refreshAndFindFileByPath("E:\\java_code\\leetcode-runner\\src\\main\\java\\com\\xhf\\leetcode\\plugin\\window\\LCToolWindowFactory.java");

        open(file2, fileEditorManager, project);
    }

    private void open(VirtualFile file, FileEditorManager fileEditorManager, Project project) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                fileEditorManager.openTextEditor(ofd, false);
            }
        });

    }
}
