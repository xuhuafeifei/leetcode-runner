package com.xhf.leetcode.plugin.utils;

import com.xhf.leetcode.plugin.model.I18nTypeEnum;
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
    private static volatile ResourceBundle bundle;
    private static volatile I18nTypeEnum i18N;

    private static final Object INIT_LOCK = new Object();

    private BundleUtils() {
    }

    private static void initBundle() {
        if (bundle == null) {
            synchronized (INIT_LOCK) {
                if (bundle == null) {
                    String locale = AppSettings.getInstance().getLocale();
                    i18N = I18nTypeEnum.getI18N(locale);
                    bundle = ResourceBundle.getBundle(I18N, i18N.getLocal());
                }
            }
        }
    }

    public static String i18n(@PropertyKey(resourceBundle = "messages.info") String key) {
        initBundle();
        return bundle.getString(key);
    }

    /**
     * 获取指定键对应的本地化字符串，并用给定参数替换占位符。
     *
     * @param key 资源键
     * @param params 替换占位符的参数
     * @return 格式化后的本地化字符串
     */
    @Deprecated
    public static String i18n(String key, Object... params) {
        initBundle();
        String messagePattern = bundle.getString(key);
        return java.text.MessageFormat.format(messagePattern, params);
    }

    /**
     * 总是将国际化内容写入properties文件太累了, 提供一个helper函数
     * <p>
     * 该方法的好处是不需要将内容写入properties文件, 但坏处是硬编码. 但我不管了, 对于那些万年不变的代码怎么方便怎么来
     *
     * @param cn 中文
     * @param en 英文
     * @return content
     */
    public static String i18nHelper(String cn, String en) {
        if (i18N == I18nTypeEnum.ZH) {
            return cn;
        } else {
            return en;
        }
    }
}
