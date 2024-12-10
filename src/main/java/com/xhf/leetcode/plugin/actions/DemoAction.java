package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.utils.LogUtils;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DemoAction extends AbstractAction {
    @Override
    void doActionPerformed(Project project, AnActionEvent e)  {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        if (1 != 2) {
            // 加载图片资源
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/jiejie.jpg"));

            // 创建一个 JPanel 来容纳图片和文本
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel(icon));
            panel.add(new JLabel("这是作者编写的第一个demoAction, 保留只为做纪念, 不会执行任何逻辑, 桀桀"));

            // 显示自定义对话框
            int result = JOptionPane.showOptionDialog(
                    null,
                    panel,
                    "桀桀",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{"确定", "取消"},
                    "确定"
            );
            new Thread(() -> {
                // 获取音频文件的 URL
                java.net.URL url = getClass().getResource("/MP3/hahaha.mp3");
                if (url == null) {
                    LogUtils.warn("路径未找到...");
                    return;
                }
                LogUtils.info("准备播放音频");
                // 打开音频输入流
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(url)) {
                    // 获取音频格式
                    AudioFormat format = audioStream.getFormat();
                    // 获取数据行信息
                    DataLine.Info info = new DataLine.Info(Clip.class, format);
                    // 打开数据行
                    Clip clip = (Clip) AudioSystem.getLine(info);
                    clip.open(audioStream);
                    // 开始播放
                    clip.start();
                    // 等待音频播放完成
                    while (!clip.isRunning())
                        Thread.sleep(10);
                    while (clip.isRunning())
                        Thread.sleep(10);
                }catch (UnsupportedAudioFileException | IOException | LineUnavailableException |
                         InterruptedException ex) {
                    LogUtils.error("播放音频失败: ", ex);
                }
            }).start();
            return;
        }

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
