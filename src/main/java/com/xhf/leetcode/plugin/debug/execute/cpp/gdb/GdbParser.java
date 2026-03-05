package com.xhf.leetcode.plugin.debug.execute.cpp.gdb;

import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * 解析gdb-mi的输出
 * 学习模仿{@link com.google.gson.Gson}的解析数据结构, 采用递归的方式返回解析数据
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class GdbParser {

    // 单例
    private static final GdbParser INSTANCE = new GdbParser();

    private GdbParser() {
    }

    public static GdbParser getInstance() {
        return INSTANCE;
    }

    /**
     * 预处理GDB的输出, 如果GDB的输出是a=b,c=d这样连续的键值对, 而没有使用{}或[]等符号, 那么最终解析得不到正确的结果
     *
     * @return s
     */
    public String preHandle(String basic) {
        String input = basic;
        if (StringUtils.isBlank(input)) {
            return input;
        }
        input = input.trim();
        String prefix = "";
        if (input.startsWith("^") || input.startsWith("*") || input.startsWith("=")) {
            // 去掉前缀（如 ^done, *stopped, =thread-group-added）
            int endIndex = input.indexOf(',');
            prefix = input.substring(0, endIndex + 1);
            input = input.substring(endIndex + 1).trim();
        }
        char c = input.charAt(0);
        if (c != '{') {
            return prefix + "{" + input + "}";
        } else {
            return basic;
        }
    }

    public GdbElement parse(String input) {
        if (input.startsWith("^")) {
            return parseResultClass(input);
        }
        // 尝试着让parseResultClass解析
        return parseResultClass(input);
    }

    public GdbElement parseResultClass(String input) {
        input = input.trim();
        if (StringUtils.isBlank(input)) {
            return new GdbPrimitive(input);
        }
        if (input.startsWith("^") || input.startsWith("*") || input.startsWith("=")) {
            // 去掉前缀（如 ^done, *stopped, =thread-group-added）
            input = input.substring(input.indexOf(',') + 1).trim();
        }

        if (input.startsWith("[")) {
            // 解析列表
            return parseGdbArray(input);
        } else if (input.startsWith("{") || isKeyValue(input)) {
            // 解析对象
            return parseGdbObject(input);
        } else {
            // 解析基本数据类型
            return parseGdbPrimitive(input);
        }
    }

    private boolean isKeyValue(String input) {
        // 尝试获取第一个key
        int i = CharacterHelper.startVNameLen(input);
        if (i >= input.length()) {
            return false;
        }
        return input.charAt(i) == '=';
    }

    private GdbElement parseGdbPrimitive(String input) {
        input = DebugUtils.removeQuotes(input);
        return new GdbPrimitive(input);
    }

    private GdbElement parseGdbObject(String input) {
        GdbObject gdbObject = new GdbObject();
        if (!input.startsWith("{")) {
            // key - value
            int len = CharacterHelper.startVNameLen(input);
            String key = input.substring(0, len);
            String value = input.substring(len + 1);
            gdbObject.add(key, parse(value));
            return gdbObject;
        }
        input = input.substring(1, input.length() - 1);
        doParse(input.toCharArray(), gdbObject);
        return gdbObject;
    }

    private GdbElement parseGdbArray(String input) {
        // 去除两端括号
        input = input.substring(1, input.length() - 1);
        GdbArray gdbArray = new GdbArray();
        doParse(input.toCharArray(), gdbArray);
        return gdbArray;
    }

    /**
     * 递归解析数组, 通过传出参数来实现返回解析出的内容
     *
     * @param arr 需要解析的内容
     * @param ele 解析结果
     */
    private void doParse(char[] arr, GdbElement ele) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '{') {
                int end;
                try {
                    end = CharacterHelper.matchBracket(arr, i);
                } catch (DebugError ignored) {
                    // 发生异常, 很可能是GDB的返回的数据存在问题, md离谱, 比如下方数据
                    /*
                      ^done,variables=[{name="solution",type="Solution"},{name="a0",type="std::string"},{name="a1",type="std::string"},{name="a2",type="std::vector<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> > > >"}]
                      在靠右端的数据中存在如下错误括号: {name="a2",value="std::vector of length -609310409, capacity 1 = {<error reading variable: Cannot access memory at address 0x0>"}
                     */
                    break;
                }
                for (int j = i; j < end; j++) {
                    sb.append(arr[j]);
                }
                i = end - 1;
            } else if (arr[i] == '[') {
                int end;
                try {
                    end = CharacterHelper.matchBracket(arr, i);
                } catch (DebugError ignored) {
                    break;
                }
                for (int j = i; j < end; j++) {
                    sb.append(arr[j]);
                }
                i = end - 1;
            } else if (arr[i] == ',') {
                doAdd(sb, ele);
            } else if (arr[i] == '\"') {
                // 去除""内部的所有信息,包括匹配的另一个"
                int j = i + 1;
                while (j < arr.length && arr[j] != '"') {
                    ++j;
                }
                for (int k = i; k <= j; k++) {
                    sb.append(arr[k]);
                }
                i = j;
            } else {
                sb.append(arr[i]);
            }
        }
        if (sb.length() != 0) {
            doAdd(sb, ele);
        }
    }

    /**
     * 真正添加的方法, 判断ele类型
     *
     * @param sb sb
     * @param ele ele
     */
    private void doAdd(StringBuilder sb, GdbElement ele) {
        String str = sb.toString();
        if (ele.isGdbObject()) {
            int len = CharacterHelper.startVNameLen(str);
            ele.getAsGdbObject().add(str.substring(0, len), parse(str.substring(len + 1)));
        } else if (ele.isGdbArray()) {
            ele.add(parse(str));
        }
        sb.delete(0, sb.length());
    }
}
