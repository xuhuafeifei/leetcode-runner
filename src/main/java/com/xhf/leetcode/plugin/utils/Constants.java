package com.xhf.leetcode.plugin.utils;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public interface Constants {
    Color FOREGROUND_COLOR = UIManager.getColor("Label.foreground");
    Color BACKGROUND_COLOR = UIManager.getColor("Table.background");
    Font ENGLISH_FONT = new Font("DejaVu Sans Mono", Font.PLAIN, 15);
    Font CN_FONT = new Font("Monospaced", Font.PLAIN, 15);
    String SOLUTION_OPEN_ERROR = "Submission Editor open error! please close all file and try again";
    String SOLUTION_CONTENT_NOT_SUPPORT = "This content is not supported to show yet..., please choose another solution";

    Color GREEN_COLOR = new JBColor(new Color(92, 184, 92), new Color(92, 184, 92));
    Color YELLOW_COLOR = new JBColor(new Color(240, 173, 78), new Color(240, 173, 78));
    Color RED_COLOR = new JBColor(new Color(217, 83, 79), new Color(217, 83, 79));

    String PLEASE_LOGIN = "Please Login First...";
    String NOTING_TO_SHOW = "Noting To Show";

    String SUBMISSION_EDITOR_OPEN_ERROR = "Submission Editor open error! please close all file and try again";
}
