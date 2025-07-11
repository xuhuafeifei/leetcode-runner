package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.xhf.leetcode.plugin.debug.execute.java.p.TreeNodePrinter;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.RandomUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TreeNodeConvertor extends AbstractVariableConvertor {

    public static void main(String[] args) {
        // String testcase = "[4,null,8,null,5,null,7,null,5,null,2,1,3,null,null,null,6,8,9,null,null,null,3,null,2,null,10,null,7,null,8,3,4,null,null,null,3,5,1,null,null,null,3,1,7,null,null,null,4,7,7,null,null,8,3,null,null,null,6,3,1,null,null,null,1,null,8,null,2,5,5,null,null,1,3,null,null,null,5,null,3,3,5,null,null,null,7,null,10,null,7,null,6,null,8,null,4,null,10,null,6,null,6,9,3,null,null,6,5,null,null,null,5,null,2,null,7,null,5,null,4,8,2,null,null,null,2,null,10,10,8,null,null,null,7,null,2,null,5,8,6,null,null,null,5,null,7,null,3,4,5,null,null,null,4,null,8,null,8,null,8,null,2,null,5,2,9,null,null,null,2,null,3,7,1,null,null,10,1,null,null,null,7,null,6,null,6,null,7,null,7,null,4,4,2,null,null,7,4,null,null,null,7,null,3,7,5,null,null,null,5,null,4,null,9,5,2,null,null,null,4,null,9,null,5,null,5,null,5,null,2,null,5,null,2,null,5,null,7,5,5,null,null,null,6,null,1,null,7,null,3,9,8,null,null,null,4,null,7,4,8,null,null,4,2,null,null,null,3,10,2,null,null,null,7,null,10,null,3,null,1,null,2,null,5,null,9,null,8,null,5,null,9,null,3,null,7,null,10,5,2,null,null,null,2,8,10,null,null,null,4,4,7,null,null,null,5,1,4,null,null,null,10,null,9,null,4,null,9,6,5,null,null,null,7,5,4,null,null,null,8,null,8,4,9,null,null,null,6,9,1,null,null,null,3,3,6,null,null,null,6,null,7,null,2,null,1,null,8,2,9,null,null,null,8,null,3,null,1,9,1,null,null,null,2,null,6,null,1,null,6,3,9,null,null,null,10,null,1,null,9,null,9,null,10,null,2,null,6,null,3,null,7,null,2,null,2,null,2,9,5,null,null,null,5,null,6,null,6,null,2,null,5,7,9,null,null,null,6,10,4,null,null,8,4,null,null,4,2,null,null,4,7,null,null,2,5,null,null,null,4,5,1,null,null,null,3,null,1,10,6,null,null,3,2,null,null,null,6,null,9,null,7,null,5,8,5,null,null,null,5,null,5,10,6,null,null,null,7,null,1,null,6,3,7,null,null,null,9,7,1,null,null,null,7,null,4,null,4,null,9,null,4,null,1,null,10,null,1,10,10,null,null,null,6,null,3,null,1,null,9,null,7,null,6,6,1,null,null,null,9,4,7,null,null,null,3,null,10,null,4,3,3,null,null,null,4,5,10,null,null,null,1,8,10,null,null,null,6,null,9,null,10,null,4,4,9,null,null,null,3,null,3,null,3,null,10,null,10,null,6,8,1,null,null,null,9,7,1,null,null,null,5,null,3,null,10,null,5,null,9,null,5,null,8,null,6,3,2,null,null,null,8,null,8,3,9,null,null,null,9,null,10,3,8,null,null,6,6,null,null,null,6,null,8,null,2,null,9,null,4,null,6,null,4,null,4,null,6,null,9,null,7,null,10,null,1,null,3,null,6,null,7,null,4,null,9,null,1,null,3,8,10,null,null,null,2,null,10,null,4,null,8,null,10,null,7,null,8,5,1,null,null,9,3,null,null,7,8,null,null,null,1,null,1,5,4,null,null,null,1,null,4,5,7,null,null,null,3,null,6,null,6,null,9,null,4,null,1,5,10,null,null,null,3,null,7,null,10,null,8,null,9,2,5,null,null,null,3,null,9,10,6,null,null,null,8,null,7,8,6,null,null,null,6,null,3,null,5,null,4,null,4,null,9,null,6,2,7,null,null,null,9,null,6,1,9,null,null,null,4,null,9,9,9,null,null,null,7,7,1,null,null,null,5,null,5,6,10,null,null,null,4,null,4,10,10,null,null,null,7,2,7,null,null,null,2,null,4,null,5,null,5,10,2,null,null,null,7,9,5,null,null,null,8,null,6,null,10,8,2,null,null,8,10,null,null,null,1,null,1,null,6,5,1,null,null,8,8,null,null,8,4,null,null,null,7,null,10,4,9,null,null,null,7,null,9,null,9,1,7,null,null,4,7,null,null,null,7,null,1,null,5,8,9,null,null,9,8,null,null,9,10,null,null,4,5,null,null,1,1,null,null,null,7,null,6,null,1,null,2,1,10,null,null,2,5,null,null,7,7,null,null,null,7,null,2,null,4,3,10,null,null,null,1,null,7,null,10,7,9,null,null,null,5,4,9,null,null,null,10,6,4,null,null,8,4,null,null,null,1,null,2,null,1,8,1,null,null,null,3,null,2,null,6,null,9,null,2,1,10,null,null,null,5,null,8,2,1,null,null,null,2,3,10,null,null,null,8,null,9,null,5,null,4,null,1,9,10,null,null,4,9,null,null,3,5,null,null,null,6,null,6,9,1,null,null,null,5,null,2,null,2,null,6,null,1,7,9,null,null,null,6,null,8,4,4,null,null,null,2,null,10,null,1,null,2,null,9,null,8,null,2,null,1,10,4,null,null,null,10,null,8,3,2,null,null,null,10,null,3,8,1,null,null,5,3,null,null,null,6,null,8,null,7,2,5,null,null,1,6,null,null,null,8,null,6,null,3,null,8,null,9,null,5,null,2,null,9,null,2,6,10,null,null,7,10,null,null,null,6,null,8,null,7,7,4,null,null,null,3,5,2,null,null,10,4,null,null,null,4,4,3,null,null,null,5,null,1,null,10,null,10,null,5,null,9,null,3,null,8,null,3,null,2,null,4,1,1,null,null,null,7,10,8,null,null,null,9,4,8,null,null,1,2,null,null,9,7,null,null,5,8,null,null,null,9,null,7,null,4,null,4,5,3,null,null,null,2,null,4,3,10,null,null,7,7,null,null,null,2,null,2,8,8,null,null,null,2,null,4,null,5,8,4,null,null,null,9,null,4,null,10,null,4,null,5,null,5,null,1,null,5,null,8,null,5,null,5,null,1,null,10,null,9,null,10,null,2,null,7,5,9,null,null,null,6,4,6,null,null,null,2,null,10,null,1,4,3,null,null,7,8,null,null,null,3,null,3,null,8,null,10,null,6,6,10,null,null,null,1,8,5,null,null,1,3,null,null,null,8,null,9,null,10,null,8,4,9,null,null,10,1,null,null,null,2,null,8,5,2,null,null,8,6,null,null,null,4,null,7,10,1,null,null,null,3,3,3,null,null,null,3,null,5,7,3,null,null,10,9,null,null,null,2,null,8,null,10,null,7,null,10,null,3,9,10,null,null,null,6,4,9,null,null,9,3,null,null,null,7,null,2,null,10,null,10,null,7,null,4,5,7,null,null,9,8,null,null,null,6,3,1,null,null,null,9,null,7,4,4,null,null,null,6,null,1,null,9,null,9,null,3,1,1,null,null,1,8,null,null,null,1,null,2,null,7,4,6,null,null,null,1,null,3,null,8,null,10,null,3,null,10,null,10,null,10,null,10,null,10,null,6,null,7,null,3,null,9,null,7,5,4,null,null,null,5,null,5,1,3,null,null,null,6,3,4,null,null,null,3,2,10,null,null,10,5,null,null,null,5,9,1,null,null,null,8,null,7,null,9,5,3,null,null,null,2,null,7,null,10,null,2,9,4,null,null,null,4,10,10,null,null,null,6,2,6,null,null,null,4,null,5,null,7,null,7,null,2,4,1,null,null,null,7,null,5,8,8,null,null,3,6,null,null,null,1,null,5,null,8,4,6,null,null,null,6,null,9,null,4,null,4,null,3,null,2,6,9,null,null,null,6,6,8,null,null,null,7,null,5,null,5,null,9,4,3,null,null,null,10,4,6,null,null,null,9,null,3,null,10,null,9,null,1,null,6,null,1,null,4,null,5,5,3,null,null,7,8,null,null,null,6,null,6,null,5,9,4,null,null,null,9,null,7,null,7,7,5,null,null,null,7,null,3,8,3,null,null,null,1,5,4,null,null,null,2,null,3,null,4,null,5,5,6,null,null,null,2,null,2,7,9,null,null,9,5,null,null,null,9,null,9,null,8,7,6,null,null,null,2,null,9,null,2,null,7,6,4,null,null,null,1,null,7,null,2,null,7,null,3,9,2,null,null,4,5,null,null,null,3,null,2,null,8,7,8,null,null,7,7,null,null,null,10,null,9,2,7,null,null,6,3,null,null,null,10,null,5,null,7,null,9,null,3,null,1,null,9,null,2,5,2,null,null,null,4,null,8,null,6,10,10,null,null,10,3,null,null,null,3,null,1,null,3,null,8,3,2,null,null,null,5,2,8,null,null,7,5,null,null,7,7,null,null,null,1,6,5,null,null,null,2,null,4,null,7,null,5,null,3,null,7,null,10,null,10,null,7,null,9,null,5,5,5,null,null,null,9,null,4,null,7,null,6,null,2,null,3,null,3,8,10,null,null,null,1,null,3,null,6,null,10,null,8,null,6,4,5,null,null,null,6,null,3,null,3,null,8,1,3,null,null,2,3,null,null,null,7,null,10,null,2,null,10,null,2,null,7,null,10,6,7,null,null,3,4,null,null,6,2,null,null,null,9,null,8,null,7,null,10,null,9,10,1,null,null,null,5,1,10,null,null,10,2,null,null,null,2,5,8,null,null,null,9,8,8,null,null,null,8,null,4,1,3,null,null,null,4,null,4,null,9,null,4,10,7,null,null,10,4,null,null,4,5,null,null,9,2,null,null,3,7,null,null,8,7,null,null,null,5,null,10,null,3,null,8,null,3,null,5,2,9,null,null,null,10,null,3,null,10,null,7,5,1,null,null,2,4,null,null,null,5,null,2,null,6,null,8,null,9,null,10,null,9,null,6,null,2,6,7,null,null,2,7,null,null,null,3,null,6,9,5,null,null,2,6,null,null,null,8,null,4,null,8,null,2,4,9,null,null,4,7,null,null,null,9,null,5,null,3,null,8,null,6,null,5,7,4,null,null,8,7,null,null,null,9,1,2,null,null,null,9,6,7,null,null,null,8,null,6,null,6,4,6,null,null,null,3,null,5,10,4,null,null,null,5,null,8,null,8,null,7,null,10,5,10,null,null,null,10,null,10,null,10,8,2,null,null,5,3,null,null,null,8,6,6,null,null,10,8,null,null,1,8,null,null,null,9,1,6,null,null,7,6,null,null,null,10,5,4,null,null,null,10,5,4,null,null,2,5,null,null,null,4,2,5,null,null,null,3,null,4,2,8,null,null,null,5,null,9,null,3,9,3,null,null,null,5,null,7,null,7,null,5,null,10,null,3,2,7,null,null,3,8,null,null,null,10,2,3,null,null,null,7,3,3,null,null,null,6,null,4,null,8,null,3,null,3,null,1,null,9,10,1,null,null,null,1,null,6,6,5,null,null,6,3,null,null,null,6,null,4,null,2,null,10,null,9,2,5,null,null,null,10,null,10,3,5,null,null,10,6,null,null,1,9,null,null,6,7,null,null,6,5,null,null,null,8,null,8,5,6,null,null,null,6,null,8,null,8,null,4,null,6,null,9,null,2,null,1,null,10,null,9,null,9,null,4,1,6,null,null,null,1,null,3,null,4,10,8,null,null,null,7,null,5,null,10,null,1,null,9,null,9,null,9,1,8,null,null,null,1,null,9,5,1,null,null,7,1,null,null,null,8,null,1,8,6,null,null,2,9,null,null,10,5,null,null,null,2,null,10,null,10,null,9,null,10,null,7,null,7,null,5,null,8,8,2,null,null,null,9,null,10,null,1,null,1,null,1,null,10,6,1,null,null,null,9,null,2,9,9,null,null,null,9,3,8,null,null,null,1,null,10,1,10,null,null,null,8,null,7,null,8,null,8,6,5,null,null,2,5,null,null,null,7,null,1,null,10,null,4,8,5,null,null,5,2,null,null,2,3,null,null,null,6,3,10,null,null,1,8,null,null,null,9,null,8,7,10,null,null,null,10,null,5,10,6,null,null,null,5,null,6,null,5,6,6,null,null,5,8,null,null,null,7,null,8,null,10,1,1,null,null,null,10,1,2,null,null,9,5,null,null,null,7,4,5,null,null,null,10,null,3,null,5,null,8,null,2,null,9,null,9,6,7,null,null,7,1,null,null,null,5,null,2,null,8,5,3,null,null,null,7,null,6,null,6,null,7,null,5,null,1,6,7,null,null,null,6,null,8,null,8,null,5,10,10,null,null,null,10,5,2,null,null,6,5,null,null,8,1,null,null,2,3,null,null,9,3,null,null,10,7,null,null,1,4,null,null,5,10,null,null,null,7,null,6,null,1,null,9,null,8,null,2,10,7,null,null,null,5,3,9,null,null,null,2,null,7,null,3,null,7,null,7,9,2,null,null,null,5,null,6,1,2,null,null,5,10,null,null,6,9,null,null,null,10,9,8,null,null,5,9,null,null,null,10,5,6,null,null,null,10,10,1,null,null,null,7,null,10,null,3,null,2,null,6,9,9,null,null,2,5,null,null,null,1,null,8,null,2,null,4,2,9,null,null,null,10,null,6,null,5,2,3,null,null,null,1,null,7,null,10,6,10,null,null,null,2,5,9,null,null,4,7,null,null,null,2,1,1,null,null,null,9,null,5,7,7,null,null,null,3,null,4,null,10,2,6,null,null,8,6,null,null,1,10,null,null,null,10,4,4,null,null,null,7,null,8,7,5,null,null,null,2,10,6,null,null,3,6,null,null,null,10,null,8,null,8,8,9,null,null,null,7,null,8,null,1,null,5,null,8,null,7,10,6,null,null,null,3,null,5,null,6,9,10,null,null,null,10,null,6,null,2,null,2,null,2,null,9,null,7,null,4,5,9,null,null,null,4,null,4,null,3,null,10,null,3,10,3,null,null,5,7,null,null,null,6,null,3,3,4,null,null,null,7,null,6,null,10,null,5,8,8,null,null,null,4,5,5,null,null,null,2,null,10,null,2,null,1,2,8,null,null,null,5,null,8,null,3,null,4,null,8,null,1,null,5,8,1,null,null,3,9,null,null,null,3,1,1,null,null,5,9,null,null,null,6,null,9,5,6,null,null,null,5,3,5,null,null,null,9,3,1,null,null,3,5,null,null,3,10,null,null,null,10,null,8,null,1,null,7,null,4,null,1,null,7,null,1,null,3,7,9,null,null,1,2,null,null,null,8,3,7,null,null,null,8,null,1,6,6,null,null,null,9,7,4,null,null,6,10,null,null,4,5,null,null,null,1,null,7,null,6,7,3,null,null,null,6,null,9,null,8,2,6,null,null,6,8,null,null,2,7,null,null,null,8,null,8,7,5,null,null,null,4,null,9,5,3,null,null,9,5,null,null,null,5,null,1,null,5,null,6,8,6,null,null,null,5,null,4,null,2,8,5,null,null,null,9,null,5,null,9,null,3,null,5,9,3,null,null,null,2,null,7,null,8,null,8,null,8,null,10,7,2,null,null,null,6,null,2,1,10,null,null,null,6,null,8,null,4,null,6,8,5,null,null,null,3,null,1,null,6,null,6,null,2,null,9,1,9,null,null,null,3,null,7,4,7,null,null,9,6,null,null,7,8,null,null,null,1,5,1,null,null,7,10,null,null,null,6,null,8,3,2,null,null,1,5,null,null,null,8,null,3,null,3,9,1,null,null,null,8,null,1,3,5,null,null,null,9,null,8,3,4,null,null,null,9,null,1,null,3,null,7,null,3,5,1,null,null,null,4,null,1,null,5,null,1,null,3,4,8,null,null,null,1,10,7,null,null,null,1,null,9,null,7,null,3,null,10,6,9,null,null,null,3,6,8,null,null,null,8,null,3,null,4,null,10,null,2,10,7,null,null,5,4,null,null,null,4,2,6,null,null,1,10,null,null,null,4,3,7,null,null,null,4,null,1,null,6,null,10,null,7,4,9,null,null,null,10,9,4,null,null,null,6,5,9,null,null,null,7,1,7,null,null,null,4,null,4,null,4,null,6,4,3,null,null,null,4,null,5,null,10,null,2,null,1,null,1,null,2,null,2,9,4,null,null,null,9,null,9,9,4,null,null,null,5,null,6,null,2,null,3,null,10,9,10,null,null,10,2,null,null,3,9,null,null,null,9,null,10,null,9,null,3,null,1,5,6,null,null,null,6,null,2,null,9,null,3,null,9,9,3,null,null,5,3,null,null,null,2,null,3,null,8,null,2,null,9,null,3,null,4,null,3,null,4,null,8,6,7,null,null,null,6,null,3,null,1,null,9,5,1,null,null,null,2,null,7,4,7,null,null,null,2,null,9,null,7,null,10,null,6,null,7,null,1,null,4,null,5,null,2,null,7,null,3,null,7,null,4,null,5,null,10,null,1,null,9,9,8,null,null,10,10,null,null,null,6,6,10,null,null,10,4,null,null,4,6,null,null,null,4,null,3,null,5,4,8,null,null,null,5,6,3,null,null,1,7,null,null,9,4,null,null,null,9,10,2,null,null,null,5,null,6,2,5,null,null,null,10,5,1,null,null,null,8,2,2,null,null,7,6,null,null,null,9,null,4,null,4,null,2,null,4,null,8,1,10,null,null,null,8,null,3,null,1,null,5,null,2,null,9,8,5,null,null,8,6,null,null,null,1,null,6,null,2,2,9,null,null,null,9,5,7,null,null,null,4,null,5,4,5,null,null,1,1,null,null,8,3,null,null,null,10,7,10,null,null,6,5,null,null,6,3,null,null,4,1,null,null,10,1,null,null,4,2,null,null,6,3,null,null,null,2,null,9,null,10,9,9,null,null,null,2,null,8,null,8,6,2,null,null,10,7,null,null,null,10,1,3,null,null,2,3,null,null,null,10,3,1,null,null,null,9,null,4,null,3,null,4,null,7,null,2,null,1,null,9,null,1,null,7,null,9,null,7,null,6,7,9,null,null,null,10,null,6,3,2,null,null,null,4,null,4,null,5,4,1,null,null,null,3,null,3,null,6,null,5,null,4,10,5,null,null,4,6,null,null,10,4,null,null,null,7,null,10,null,1,null,1,5,6,null,null,9,7,null,null,null,3,null,6,null,8,null,2,null,4,null,2,null,7,null,8,3,10,null,null,null,6,null,3,null,7,null,4,2,3,null,null,1,9,null,null,5,6,null,null,6,6,null,null,null,7,null,8,9,9,null,null,null,9,null,1,null,9,null,5,null,1,null,5,2,6,null,null,null,9,2,4,null,null,3,6,null,null,4,2,null,null,null,9,null,6,3,3,null,null,null,7,null,9,null,6,2,9,null,null,null,8,null,5,null,4,null,7,null,4,null,8,null,5,3,2,null,null,null,1,null,1,null,1,null,1,null,1,null,1,null,4,null,6,3,7,null,null,9,7,null,null,9,2,null,null,null,4,null,1,null,5,null,8,null,2,10,1,null,null,null,10,null,1,2,7,null,null,null,5,null,8,null,7,2,6,null,null,null,10,null,3,null,7,null,3,null,6,9,4,null,null,null,2,null,8,null,8,null,1,null,8,null,8,null,9,null,7,null,5,10,9,null,null,4,4,null,null,null,7,null,6,null,8,1,3,null,null,9,3,null,null,1,10,null,null,null,9,8,2,null,null,null,8,null,4,null,4,4,1,null,null,null,7,null,8,1,9,null,null,null,10,null,3,6,7,null,null,null,5,5,2,null,null,null,4,null,5,6,6,null,null,7,7,null,null,5,10,null,null,null,6,10,6,null,null,null,2,null,5,2,5,null,null,null,7,null,7,null,7,null,4,null,9,null,4,null,8,null,1,null,5,null,9,6,4,null,null,null,8,9,2,null,null,10,2,null,null,null,3,null,4,null,10,null,6,null,10,2,9,null,null,6,1,null,null,null,7,6,6,null,null,null,2,null,4,null,10,8,9,null,null,null,7,3,9,null,null,10,4,null,null,null,10,null,6,null,2,null,5,null,1,null,8,8,3,null,null,null,2,5,10,null,null,5,8,null,null,3,10,null,null,null,5,null,8,null,5,null,4,null,5,6,2,null,null,null,7,null,5,null,10,null,8,1,5,null,null,null,1,null,1,null,5,null,9,null,6,null,1,null,5]";
        String testcase = "[1,null,10,1,9,7]";
        testcase = testcase.trim();

        TreeNode head = null;
        if (!"[]".equals(testcase)) {
            // 把collect变为数组
            Integer[] split =
                Arrays.stream(
                        testcase.replace("[", "")
                            .replace("]", "")
                            .split(","))
                    .map(e -> "null".equals(e) ? null : Integer.parseInt(e))
                    .toArray(Integer[]::new);
            int i = 0;
            head = new TreeNode(split[i]);
            i++;
            Queue<TreeNode> q = new LinkedList<>();
            q.add(head);

            while (!q.isEmpty()) {
                TreeNode node = q.poll();
                // 添加它的左节点
                if (i < split.length) {
                    if (split[i] != null) {
                        node.left = new TreeNode(split[i]);
                        q.add(node.left);
                    }
                    i += 1;
                }
                // 添加右节点
                if (i < split.length) {
                    if (split[i] != null) {
                        node.right = new TreeNode(split[i]);
                        q.add(node.right);
                    }
                    i += 1;
                }
            }
        }
        TreeNodePrinter treeNodePrinter = new TreeNodePrinter(head);
        treeNodePrinter.visitAndPrint();
    }

    // dfs遍历TreeNode
    public static void dfs(TreeNode root) {
        if (root == null) {
            return;
        }
        System.out.println(root.val);
        dfs(root.left);
        dfs(root.right);
    }

    @Override
    protected String doCpp(String testcase, String variableName) {
        String code = FileUtils.readContentFromFile(getClass().getResource("/debug/cpp/TreeNodeConvertor.template"));
        code = code.replace("{{testcase}}", testcase)
            .replace("{{variableName}}", variableName)
            .replace("{{testcaseVName}}", RandomUtils.nextString(10));
        return addTab(code);
    }

    @Override
    protected String doPython(String testcase, String variableName) {
        String code = FileUtils.readContentFromFile(getClass().getResource("/debug/python/TreeNodeConvertor.template"));
        code = code.replace("{{testcase}}", testcase)
            .replace("{{variableName}}", variableName)
            .replace("{{testcaseVName}}", RandomUtils.nextString(10));
        return addTab(code);
    }

    @Override
    protected String doJava(String testcase, String variableName) {
        String code = FileUtils.readContentFromFile(getClass().getResource("/debug/java/TreeNodeConvertor.template"));
        code = code.replace("{{testcase}}", testcase)
            .replace("{{variableName}}", variableName)
            .replace("{{testcaseVName}}", RandomUtils.nextString(10));
        return code;
    }
}
