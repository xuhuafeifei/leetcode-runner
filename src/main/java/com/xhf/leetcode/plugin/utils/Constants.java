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
    String ARTICLE_CONTENT = "ARTICLE_CONTENT";
    String ARTICLE_TITLE = "ARTICLE_TITLE";
    String STATUS = "STATUS";
    String TOPIC_ID = "TOPIC_ID";
    String SOLUTION_SLUG = "SOLUTION_SLUG";
    String SUBMISSION_ID = "SUBMISSION_ID";
    String QUESTION_CONTENT = "QUESTION_CONTENT";
    String ARTICLE_URL = "ARTICLE_URL";

    Border BORDER = JBUI.Borders.customLine(JBColor.border(), 1);
    // 别改, debug web端代码已经写死了, 改了容易出bug
    String LOCAL_VARIABLE = BundleUtils.i18nHelper("局部变量", "Local variable");
    String STATIC_VARIABLE = BundleUtils.i18nHelper("静态变量", "Static variable");
    String MEMBER_VARIABLE = BundleUtils.i18nHelper("成员变量", "Member variable");
    String WATCH = BundleUtils.i18nHelper("监视变量", "Watch variable");


    String PY_SERVER_DISCONNECT = "PY_SERVER_DISCONNECT";
    String HELP_INFO =
            BundleUtils.i18nHelper(
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
                    ,
            "Help document format: command name [command input format] command function\n" +
            "\n" +
            "N command [n | n number | n number] step into single step execution\n" +
            "\n" +
            "R command [r] run code until next breakpoint\n" +
            "\n" +
            "P command [p | p expression] print local variable, if p command has expression, print local variable and calculate expression\n" +
            "\n" +
            "B command [b number] set breakpoint on specified line\n" +
            "\n" +
            "SHOWB command [show b | s b | sb] show all breakpoints\n" +
            "\n" +
            "RB command [remove b number|r b number|remove bnumber|r bnumber|rbnumber] remove specified line breakpoint\n" +
            "\n" +
            "RBA command [remove all|ra|r a] remove all breakpoints\n" +
            "\n" +
            "W command [w] show current position\n" +
            "\n" +
            "STEP command [step out | step over] function and idea debug corresponding button function\n" +
            "\n" +
            "WATCH command [watch expression] calculate expression and add it to watch pool"
            );


    /**
     * PythonDebugger用于在UI读取指令状态下, 通知python server终止断点初始化的operation
     */
    String OPT_PYTHON_INIT_BREAKPOINT_DONE = "PYTHON_INIT_BREAKPOINT_DONE";
    String OPEN_ON_WBE = "[open_on_web]";
}
