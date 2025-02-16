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
    String SOLUTION_OPEN_ERROR = "提交记录显示功能的编辑器打开错误! 请重定位文件或关闭当前文件";
    String SOLUTION_CONTENT_NOT_SUPPORT = "当前题解暂不支持显示, 请选择其他题解";

    Color GREEN_COLOR = new JBColor(new Color(92, 184, 92), new Color(92, 184, 92));
    Color YELLOW_COLOR = new JBColor(new Color(240, 173, 78), new Color(240, 173, 78));
    Color RED_COLOR = new JBColor(new Color(217, 83, 79), new Color(217, 83, 79));

    String PLEASE_LOGIN = "请先登录...";
    String NOTING_TO_SHOW = "没有可用于展示的数据";

    String SUBMISSION_EDITOR_OPEN_ERROR = "提交记录显示功能的编辑器打开错误! 请重定位文件或关闭当前文件";

    /*-------------用于openSecond(Map)的key--------------*/
    String SOLUTION_CONTENT = "SOLUTION_CONTENT";
    String FRONTEND_QUESTION_ID = "FRONTEND_QUESTION_ID";
    String TRANSLATED_TITLE = "TRANSLATED_TITLE";
    String DIFFICULTY = "DIFFICULTY";
    String TITLE_SLUG = "TITLE_SLUG";
    String VFILE = "VFILE";
    String STATUS = "STATUS";
    String TOPIC_ID = "TOPIC_ID";
    String SOLUTION_SLUG = "SOLUTION_SLUG";
    String SUBMISSION_ID = "SUBMISSION_ID";
    String QUESTION_CONTENT = "QUESTION_CONTENT";
    String ARTICLE_URL = "ARTICLE_URL";

    Border BORDER = JBUI.Borders.customLine(JBColor.border(), 1);
    // 别改, debug web端代码已经写死了, 改了容易出bug
    String LOCAL_VARIABLE = "Local variable";
    String STATIC_VARIABLE = "Static variable";
    String MEMBER_VARIABLE = "Member variable";
    String WATCH = "Watch variable";


    String PY_SERVER_DISCONNECT = "PY_SERVER_DISCONNECT";
    String HELP_INFO =
            "帮助文档格式: 指令名词 [指令输入形式] 指令作用\n" +
            "\n" +
            "N指令 [n | n 数字 | n 数字]  step into单步执行\n" +
            "\n" +
            "R指令 [r] 运行代码, 直到下一个断点\n" +
            "\n" +
            "P指令 [p | p expression] 打印本地变量, 如果p指令存在表达式, 在打印本地变量的同时计算表达式\n" +
            "\n" +
            "B指令 [b 数字] 在指定行打上断点\n" +
            "\n" +
            "SHOWB指令 [show b | s b | sb] 显示所有断点\n" +
            "\n" +
            "RB指令 [remove b 数字|r b 数字|rb 数字|remove b数字|r b数字|rb数字] 移除指定行断点\n" +
            "\n" +
            "RBA指令 [remove all|ra|r a]移除所有断点\n" +
            "\n" +
            "W指令 [w] 查看当前所在位置\n" +
            "\n" +
            "STEP指令 [step out | step over] 功能和idea的debug对应按钮功能一致\n" +
            "\n" +
            "WATCH指令 [watch expression] 计算expression, 同时将expression加入监视池"
            ;


    /**
     * PythonDebugger用于在UI读取指令状态下, 通知python server终止断点初始化的operation
     */
    String OPT_PYTHON_INIT_BREAKPOINT_DONE = "PYTHON_INIT_BREAKPOINT_DONE";
    String OPEN_ON_WBE = "[open_on_web]";
}
