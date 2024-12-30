package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.RandomUtils;

import java.util.Arrays;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ListNodeConvertor extends AbstractVariableConvertor {
    @Override
    protected String doPython(String testcase, String variableName) {
        return null;
    }

    @Override
    protected String doJava(String testcase, String variableName) {
        String code = FileUtils.readContentFromFile(getClass().getResource("/debug/java/ListNodeConvertor.template"));
        code = code.replace("{{testcase}}", testcase)
                   .replace("{{variableName}}", variableName)
                   .replace("{{testcaseVName}}", RandomUtils.nextString(10));
        return code;
    }

    public static void main(String[] args) {
        // [1,10,3,7,10,8,9,5,3,9,6,8,7,6,6,3,5,4,4,9,6,7,9,6,9,4,9,9,7,1,5,5,10,4,4,10,7,7,2,4,5,5,2,7,5,8,6,10,2,10,1,1,6,1,8,4,7,10,9,7,9,9,7,7,7,1,5,9,8,10,5,1,7,6,1,2,10,5,7,7,2,4,10,1,7,10,9,1,9,10,4,4,1,2,1,1,3,2,6,9]
        String testcase = "[1,10,3,7,10,8,9,5,3,9,6,8,7,6,6,3,5,4,4,9,6,7,9,6,9,4,9,9,7,1,5,5,10,4,4,10,7,7,2,4,5,5,2,7,5,8,6,10,2,10,1,1,6,1,8,4,7,10,9,7,9,9,7,7,7,1,5,9,8,10,5,1,7,6,1,2,10,5,7,7,2,4,10,1,7,10,9,1,9,10,4,4,1,2,1,1,3,2,6,9]";
        testcase = testcase.trim();

        ListNode head = null;
        if (! "[]".equals(testcase)) {
            // 把collect变为数组
            Integer[] split =
                    Arrays.stream(
                                    testcase.replace("[", "")
                                            .replace("]", "")
                                            .split(","))
                            .map(Integer::parseInt)
                            .toArray(Integer[]::new);
            int i = 0;
            head = new ListNode(split[i]);
            ListNode cp = head;
            i += 1;
            // 迭代
            for (; i < split.length; ++i) {
                cp.next = new ListNode(split[i]);
                cp = cp.next;
            }
        }
        printListNode(head);
    }

    // 遍历打印ListNode
    public static void printListNode(ListNode head) {
        while (head != null) {
            System.out.print(head.val + " ");
            head = head.next;
        }
    }
}
