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
    String LOCAL_VARIABLE = "Local variable";
    String STATIC_VARIABLE = "Static variable";
    String MEMBER_VARIABLE = "Member variable";


    String PY_SERVER_DISCONNECT = "PY_SERVER_DISCONNECT";
    String HELP_INFO =
            "帮助文档格式: 命令名词 [命令输入形式] 命令作用\n" +
            "\n" +
            "N命令 [n | n 数字 | n 数字]  step into单步执行\n" +
            "\n" +
            "R命令 [r] 运行代码, 直到下一个断点\n" +
            "\n" +
            "P命令 [p] 打印本地变量\n" +
            "\n" +
            "B命令 [b 数字] 在指定行打上断点\n" +
            "\n" +
            "SHOWB命令 [show b | s b | sb] 显示所有断点\n" +
            "\n" +
            "RB命令 [remove b 数字|r b 数字|rb 数字|remove b数字|r b数字|rb数字] 移除指定行断点\n" +
            "\n" +
            "RBA命令 [remove all|ra|r a]移除所有断点\n" +
            "\n" +
            "W命令 [w] 查看当前所在位置\n" +
            "\n" +
            "STEP命令 [step out | step over] 功能和idea的debug对应按钮功能一致\n"
            ;


    /**
     * PythonDebugger用于在UI读取指令状态下, 通知python server终止断点初始化的operation
     */
    String OPT_PYTHON_INIT_BREAKPOINT_DONE = "PYTHON_INIT_BREAKPOINT_DONE";
}
