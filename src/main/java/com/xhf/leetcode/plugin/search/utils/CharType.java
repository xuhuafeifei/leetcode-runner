package com.xhf.leetcode.plugin.search.utils;

import com.xhf.leetcode.plugin.search.process.CNProcessor;
import com.xhf.leetcode.plugin.search.process.DigitProcessor;
import com.xhf.leetcode.plugin.search.process.ENProcessor;
import com.xhf.leetcode.plugin.search.process.NonProcessor;
import com.xhf.leetcode.plugin.search.process.Processor;

public enum CharType {
    EN("en", ENProcessor.class),
    CN("cn", CNProcessor.class),
    DIGIT("digit", DigitProcessor.class),
    NON("non", NonProcessor.class),
    ;

    private final String type;
    private final Class<? extends Processor> processor;

    CharType(String type, Class<? extends Processor> processor) {
        this.type = type;
        this.processor = processor;
    }

    public String getType() {
        return type;
    }

    public Class<? extends Processor> getProcessor() {
        return processor;
    }
}
