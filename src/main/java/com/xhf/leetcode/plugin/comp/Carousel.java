package com.xhf.leetcode.plugin.comp;

import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Carousel extends JPanel {

    private final JLabel label;
    private final java.util.List<ImageIcon> images = new ArrayList<>();
    private final java.util.List<JComponent> components = new ArrayList<>();
    private final JPanel textPane;
    private int currentImageIndex = 0;

    public Carousel() {
        setLayout(new BorderLayout());

        label = new JLabel();
        textPane = new JPanel();
        textPane.setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        add(textPane, BorderLayout.SOUTH);

        // "上一张" 按钮
        var prevButton = new JRadioButton(BundleUtils.i18n("comp.leetcode.pre"));
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentImageIndex = (currentImageIndex - 1 + images.size()) % images.size();
                updateImageAndText();
            }
        });
        add(prevButton, BorderLayout.WEST);

        // "下一张" 按钮
        var nextButton = new JRadioButton(BundleUtils.i18n("comp.leetcode.next"));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentImageIndex = (currentImageIndex + 1) % images.size();
                updateImageAndText();
            }
        });
        add(nextButton, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame jFrame = new JFrame();
                jFrame.setTitle(BundleUtils.i18n("comp.leetcode.carousel"));
                jFrame.setSize(400, 300);
                jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Carousel comp = new Carousel();
                jFrame.add(comp);
                // 初始化图片资源
                comp.addImageAndPanel("E:\\java_code\\leetcode-runner\\src\\main\\resources\\icons\\star1.jpg",
                    new JTextArea("abab"));
                comp.addImage("E:\\java_code\\leetcode-runner\\src\\main\\resources\\icons\\star2.jpg");
                jFrame.setVisible(true);
            }
        });
    }

    private void updateImageAndText() {
        label.setIcon(images.get(currentImageIndex));  // 假设JPanel的第一个组件是ImageIcon
        var comp = components.get(currentImageIndex);
        textPane.removeAll();
        textPane.add(comp);
    }

    public void addImage(String imagePath) {
        images.add(new ImageIcon(imagePath));
        components.add(new JPanel());
        updateImageAndText();
    }

    public void addImageAndPanel(String imagePath, JComponent comp) {
        images.add(new ImageIcon(imagePath));
        components.add(comp);
        updateImageAndText();
    }
}