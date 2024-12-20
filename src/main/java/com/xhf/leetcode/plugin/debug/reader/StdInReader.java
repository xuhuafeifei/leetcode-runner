package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;

import java.util.Scanner;

/**
 * 从标准输入读取命令, 并解析为指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdInReader extends CommandReader{

    public StdInReader(Project project) {
        super(project, ReadType.STD_IN);
    }

    @Override
    protected String readCommand() {
        Scanner sc = new Scanner(System.in);
        String res = sc.nextLine();
        return res;
    }
}
