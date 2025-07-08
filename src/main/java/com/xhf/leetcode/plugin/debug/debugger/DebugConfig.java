package com.xhf.leetcode.plugin.debug.debugger;

import com.xhf.leetcode.plugin.debug.output.Output;
import com.xhf.leetcode.plugin.debug.reader.InstReader;

public interface DebugConfig {

    InstReader getReader();

    void setReader(InstReader reader);

    Output getOutput();

    void setOutput(Output output);
}
