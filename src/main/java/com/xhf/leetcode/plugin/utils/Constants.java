package com.xhf.leetcode.plugin.utils;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
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

    /*-------------用于openSecond(Map)的key--------------*/
    String SOLUTION_CONTENT = "SOLUTION_CONTENT";
    String FRONTEND_QUESTION_ID = "FRONTEND_QUESTION_ID";
    String TRANSLATED_TITLE = "TRANSLATED_TITLE";
    String DIFFICULTY = "DIFFICULTY";
    String TITLE_SLUG = "TITLE_SLUG";
    String TOPIC_ID = "TOPIC_ID";
    String SOLUTION_SLUG = "SOLUTION_SLUG";
    String SUBMISSION_ID = "SUBMISSION_ID";
    String QUESTION_CONTENT = "QUESTION_CONTENT";

    Border BORDER = JBUI.Borders.customLine(JBColor.border(), 1);
}
