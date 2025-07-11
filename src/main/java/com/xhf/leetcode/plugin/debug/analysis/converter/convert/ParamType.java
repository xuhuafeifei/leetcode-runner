package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

/**
 * 参数类型
 */
public enum ParamType {
    /**
     * warn: 每一个recognized中的元素尽量不要出现在多个type类型中.
     * 举个例子
     * 类型LIST_STRING可识别的类型有: List<String>, List<str>
     * STRING_ARRAY可识别的类型有: String[], List<str>
     * 对于python来说, 因为没有数组的概念, 因此理论上List<str>既可以属于LIST_STRING, 也可以属于STRING_ARRAY
     * 但通过调用getByType方法, 只会返回其中一个类型, 而后续ConverterFactory将会通过ParamType生成对应的Convertor
     * <p>
     * 这就意味着, 真正处理python变量List<str>只能有一个Convertor.
     * 如果List<str>同时属于LIST_STRING和STRING_ARRAY,
     * 那么在ConverterFactory中, 只能生成一个Convertor, 而不能生成两个Convertor
     * 如果在编写对应Convertor时, 只有其中一个convertor有处理List<str>的逻辑, 工厂创建时
     * 可能会选择另一个没有处理List<str>逻辑的Convertor, 最终产生bug
     */
    INT("int", new String[]{"int"}, new IntConvertor()),
    LONG("long", new String[]{"long"}, null),
    FLOAT("float", new String[]{"float"}, null),
    DOUBLE("double", new String[]{"double"}, null),
    BOOLEAN("boolean", new String[]{"boolean", "bool"}, null),
    CHAR("char", new String[]{"char"}, null),
    STRING("String", new String[]{"String", "str", "string"}, new StringConvertor()),
    INT_ARRAY("int_array", new String[]{"int[]", "List[int]", "vector<int>"}, new IntArrayConvertor()),
    INT_MATRIX("int_matrix", new String[]{"int[][]", "List[List[int]]", "vector<vector<int>>"},
        new IntMatrixConvertor()),
    String_ARRAY("String_array", new String[]{"String[]", "List[str]", "vector<string>"}, new StringArrayConvertor()),
    String_MATRIX("String_matrix", new String[]{"String[][]", "List[List[str]]", "vector<vector<string>>"},
        new StringMatrixConvertor()),
    LIST_STRING("List_String", new String[]{"List<String>"}, new ListStringConvertor()),
    LIST_LIST_STRING("List_List_String", new String[]{"List<List<String>>"}, new ListListStringConvertor()),
    List_List_INTEGER("List_List_Integer", new String[]{"List<List<Integer>>"}, new ListListIntegerConvertor()),
    TREE_NODE("TreeNode", new String[]{"TreeNode", "TreeNode*", "Optional[TreeNode]"}, new TreeNodeConvertor()),
    LIST_NODE("List_Node", new String[]{"ListNode", "ListNode*", "Optional[ListNode]"}, new ListNodeConvertor());

    /**
     * 不同变量的类型.所有语言的类型都通过type进行标识
     * 比如Java中, 字符串数组是String[], 但python则是List[str]. 但他们都是字符串数组
     * 考虑到项目拓展性, 对于不同语言但同一种类型的不同表示方式, 给出唯一标识
     */
    private final String type;
    /*
     * 可识别类型
     * 比如字符串数组类型, 可以通过String[], List[str]识别得到
     */
    private final String[] recognized;
    /**
     * 转换器, 用于将测试用例转换为对应语言的代码
     */
    private final VariableConvertor convertor;

    ParamType(String type, String[] rec, VariableConvertor convertor) {
        this.type = type;
        this.recognized = rec;
        this.convertor = convertor;
    }

    /**
     * 通过类型返回ParamType
     */
    public static ParamType getByType(String type) {
        for (ParamType paramType : values()) {
            // 遍历可识别列表, 如果匹配, 则返回对应类型
            for (String rec : paramType.getRecognized()) {
                if (rec.equals(type)) {
                    return paramType;
                }
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String[] getRecognized() {
        return recognized;
    }

    public VariableConvertor getConvertor() {
        return convertor;
    }
}
