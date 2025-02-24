package com.xhf.leetcode.plugin.utils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;
import java.util.ResourceBundle;

import java.text.MessageFormat;

/**
 * @author 文艺倾年
 */
public class BundleUtils extends AbstractBundle {
    @NonNls
    public static final String I18N = "messages.LLConsole";
    private static final BundleUtils INSTANCE = new BundleUtils();


    private BundleUtils() {
        super(I18N);
    }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = I18N) String key, @NotNull Object ... params) {
        String string = message(key, Locale.ENGLISH);
        if(MessageFormat.format(string,params)==null){
            return MessageFormat.format(string,params);
        }
        return INSTANCE.getMessage(key, params);
    }

    private static String message(String key, Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle(I18N,locale);
        return bundle.getString(key);
    }
}
