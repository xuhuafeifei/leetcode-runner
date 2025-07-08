package com.xhf.leetcode.plugin.model;

import com.xhf.leetcode.plugin.utils.LogUtils;
import java.util.Locale;

public enum I18nTypeEnum {
    EN("en", Locale.ENGLISH),
    ZH("zh", Locale.CHINESE);

    private final Locale local;
    private final String value;

    I18nTypeEnum(String value, Locale local) {
        this.value = value;
        this.local = local;
    }

    public static I18nTypeEnum getI18N(String selectedItem) {
        for (I18nTypeEnum value : values()) {
            if (value.getValue().equals(selectedItem)) {
                return value;
            }
        }
        LogUtils.warn("selectedItem cannot be recognized ! selectedItem = " + selectedItem);
        return EN;
    }

    public String getValue() {
        return value;
    }


    public Locale getLocal() {
        return this.local;
    }
}
