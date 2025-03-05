package com.xhf.leetcode.plugin.setting;

import org.apache.commons.lang3.StringUtils;

/**
 * 语言转换器, 用于统一语言
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LanguageConvertor {
    /*-------------------Input Type--------------------*/
    public static String STD_INPUT_EN = "Std input";
    public static String COMMAND_LINE_EN = "Command line";
    public static String UI_READ_EN = "UI read";
    public static String STD_INPUT_ZH = "从标准输入读取";
    public static String COMMAND_LINE_ZH = "从命令行读取";
    public static String UI_READ_ZH = "从UI读取";

    /*--------------------Output Type-------------------*/
    public static String STD_OUTPUT_EN = "Std display";
    public static String CONSOLE_OUTPUT_EN = "Console display";
    public static String UI_DISPLAY_EN = "UI display";
    public static String STD_OUTPUT_ZH = "标准输出显示";
    public static String CONSOLE_OUTPUT_ZH = "Console显示";
    public static String UI_DISPLAY_ZH = "UI显示";
    /*-------------------Reposition Type--------------*/
    public static String REPOSITION_DEFAULT_ZH = "按照文件代表的语言类型";
    public static String REPOSITION_DEFAULT_EN = "by file language type";
    public static String REPOSITION_SETTING_ZH = "按照设置的语言类型";
    public static String REPOSITION_SETTING_EN = "by setting language type";


    public static String[] enList = {
            STD_INPUT_EN,
            COMMAND_LINE_EN,
            UI_READ_EN,
            STD_OUTPUT_EN,
            CONSOLE_OUTPUT_EN,
            UI_DISPLAY_EN,
            REPOSITION_DEFAULT_EN,
            REPOSITION_SETTING_EN
    };

    public static String[] zhList = {
            STD_INPUT_ZH,
            COMMAND_LINE_ZH,
            UI_READ_ZH,
            STD_OUTPUT_ZH,
            CONSOLE_OUTPUT_ZH,
            UI_DISPLAY_ZH,
            REPOSITION_DEFAULT_ZH,
            REPOSITION_SETTING_ZH
    };

    public static String toEn(String zh) {
        for (int i = 0; i < zhList.length; i++) {
            if (StringUtils.equals(zh, zhList[i]) ||
                    StringUtils.equals(zh, enList[i]) // 已经是英文
            ) {
                return enList[i];
            }
        }
        throw new IllegalArgumentException("No match language found.");
    }

    public static String toZh(String en) {
        for (int i = 0; i < enList.length; i++) {
            if (StringUtils.equals(en, enList[i]) ||
                    StringUtils.equals(en, zhList[i]) // 已经是中文
            ) {
                return zhList[i];
            }
        }
        throw new IllegalArgumentException("No match language found.");
    }

    public static boolean isEqual(String s1, String s2) {
        return StringUtils.equals(toEn(s1), toEn(s2));
    }
}
