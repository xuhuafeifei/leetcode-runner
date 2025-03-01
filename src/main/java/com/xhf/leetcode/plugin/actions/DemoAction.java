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
import java.awt.*;
import java.io.IOException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DemoAction extends AbstractAction {
    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        /*
          强制为true, 而且要骗过idea的检测. 毕竟我想保留一开始的垃圾代码. 但又不想要执行. 毕竟执行了铁定报错, 我又不想改, 就这么干了
         */
        if (true) {
            // 加载图片资源
            ImageIcon imageIcon = new ImageIcon(getClass().getResource("/icons/jiejie.jpg"));

            // 创建一个 JPanel 来容纳图片和文本
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            // 居中对齐图片
            JLabel imageLabel = new JLabel(imageIcon);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER); // 居中对齐图片
            panel.add(imageLabel);

            // 创建一个 JLabel 来显示文本并设置字体
            JLabel textLabel = new JLabel("这是作者编写的第一个demoAction, 保留只为做纪念, 不会执行任何逻辑, 桀桀");
            // 设置字体，使用更好看的字体
            textLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24)); // 设置字体为微软雅黑，避免中文乱码
            textLabel.setHorizontalAlignment(SwingConstants.CENTER); // 居中对齐文字
            panel.add(textLabel);

            // 播放hahaha音频, 桀桀
            startJieJie();

            // 显示自定义对话框
            JOptionPane.showOptionDialog(
                    null,
                    panel,
                    "桀桀",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{BundleUtils.i18n("action.leetcode.plugin.ok"), BundleUtils.i18n("action.leetcode.plugin.cancel")},
                    BundleUtils.i18n("action.leetcode.plugin.ok")
            );
            return;
        }
        System.out.println("abab");
        if (true) {
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

    public void startJieJie() {
        // 获取音频文件的 URL
        java.net.URL url = getClass().getResource("/MP3/hahaha.wav");
        if (url == null) {
            LogUtils.warn("路径未找到...");
            return;
        }
        new Thread(() -> {
            LogUtils.info("准备播放音频");
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(url)) {
                AudioFormat format = audioStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(audioStream);

                // 设置音量为最大
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(0);  // 设置音量为最大

                clip.start();

                // 等待播放完成
                while (clip.isRunning()) {
                    Thread.sleep(10);
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException |
                     InterruptedException ex) {
                LogUtils.error("播放音频失败: ", ex);
            }
        }).start();
    }
}
