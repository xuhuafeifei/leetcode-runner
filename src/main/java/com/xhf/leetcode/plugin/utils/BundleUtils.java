package com.xhf.leetcode.plugin.utils;

import com.xhf.leetcode.plugin.model.i18nTypeEnum;
import com.xhf.leetcode.plugin.setting.AppSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * @author 文艺倾年
 */
public class BundleUtils {
    @NonNls
    public static final String I18N = "messages/info";
    private static final ResourceBundle bundle;

    static {
        String locale = AppSettings.getInstance().getLocale();
        i18nTypeEnum i18N = i18nTypeEnum.getI18N(locale);
        bundle = ResourceBundle.getBundle(I18N, i18N.getLocal());
    }

    private BundleUtils() {
    }

    /**
     * 获取指定键对应的本地化字符串。
     *
     * @param key 资源键
     * @return 本地化字符串
     */
    public static String i18n(@PropertyKey(resourceBundle = "messages.info") String key) {
        return bundle.getString(key);
    }

    /**
     * 获取指定键对应的本地化字符串，并用给定参数替换占位符。
     *
     * @param key 资源键
     * @param params 替换占位符的参数
     * @return 格式化后的本地化字符串
     */
    public static String i18n(String key, Object... params) {
        String messagePattern = bundle.getString(key);
        return java.text.MessageFormat.format(messagePattern, params);
    }
}
