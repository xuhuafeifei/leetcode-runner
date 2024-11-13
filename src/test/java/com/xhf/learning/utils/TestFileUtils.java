package com.xhf.learning.utils;

import org.junit.Test;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;

public class TestFileUtils {
    @Test
    public void testWriteFile() throws Exception {
        String path = "E:\\java_code\\demoabab\\file\\abab.txt";
        FileUtils.createAndWriteFile(path, "ababa...");
    }
}
