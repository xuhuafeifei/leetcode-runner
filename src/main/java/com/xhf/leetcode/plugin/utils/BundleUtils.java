package com.xhf.leetcode.plugin.utils;

import com.xhf.leetcode.plugin.model.I18nTypeEnum;
import com.xhf.leetcode.plugin.setting.AppSettings;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author 文艺倾年
 */
public class BundleUtils {

    @NonNls
    public static final String I18N = "messages/info";
    private static volatile ResourceBundle bundle;
    private static volatile I18nTypeEnum i18N;

    private static volatile boolean initialized = false;

    private static synchronized void ensureInitialized() {
        if (!initialized) {
            String locale = AppSettings.getInstance().getLocale();
            i18N = I18nTypeEnum.getI18N(locale);
            bundle = ResourceBundle.getBundle(I18N, i18N.getLocal());
            initialized = true;
        }
    }

    public static String i18n(@PropertyKey(resourceBundle = "messages.info") String key) {
        ensureInitialized();
        return bundle.getString(key);
    }

    public static String i18nHelper(String cn, String en) {
        return i18N == I18nTypeEnum.ZH ? cn : en;
    }
}