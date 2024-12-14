import java.util.*;

class Question {
    private String frontendQuestionId;

    public Question(String number) {
        this.frontendQuestionId = number;
    }

    // 构造函数、getter、setter等略

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }
}

public class Main {
    public static void main(String[] args) {
        List<Question> ans = new ArrayList<>();

        // 添加一些Question对象
        // 测试案例 1: 纯数字
        ans.add(new Question("123"));
        ans.add(new Question("12"));
        ans.add(new Question("3"));

        // 测试案例 2: LCP 类型
        ans.add(new Question("LCP 45"));
        ans.add(new Question("LCP 5"));
        ans.add(new Question("LCP 100"));

        // 测试案例 3: LCR 类型
        ans.add(new Question("LCR 22"));
        ans.add(new Question("LCR 3"));
        ans.add(new Question("LCR 77"));

        // 测试案例 4: 面试题 类型
        ans.add(new Question("面试题 1"));
        ans.add(new Question("面试题 10"));
        ans.add(new Question("面试题 99"));

        // 测试案例 5: 混合类型
        ans.add(new Question("面试题 5"));
        ans.add(new Question("LCP 1"));
        ans.add(new Question("LCR 30"));
        ans.add(new Question("123"));

        ans.sort(new Comparator<Question>() {
            @Override
            public int compare(Question o1, Question o2) {
                String s1 = o1.getFrontendQuestionId();
                String s2 = o2.getFrontendQuestionId();

                // 判断s1和s2是否为纯数字
                boolean isNumber1 = s1.matches("\\d+");
                boolean isNumber2 = s2.matches("\\d+");

                // 如果一个是数字，排在前面
                if (isNumber1 && !isNumber2) {
                    return -1;
                } else if (!isNumber1 && isNumber2) {
                    return 1;
                }

                // 如果都是数字，直接比较数字的大小
                if (isNumber1 && isNumber2) {
                    return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
                }

                // 处理 LCP、LCR、面试题 排序
                List<String> types = Arrays.asList("LCP", "LCR", "面试题");
                int type1Index = getTypeIndex(s1, types);
                int type2Index = getTypeIndex(s2, types);

                // 如果类型不同，按类型顺序比较
                if (type1Index != type2Index) {
                    return Integer.compare(type1Index, type2Index);
                }

                // 类型相同，比较数字部分
                int num1 = Integer.parseInt(s1.replaceAll("\\D+", ""));
                int num2 = Integer.parseInt(s2.replaceAll("\\D+", ""));
                return Integer.compare(num1, num2);
            }

            // 获取类型的索引，LCP -> 0, LCR -> 1, 面试题 -> 2
            private int getTypeIndex(String s, List<String> types) {
                for (int i = 0; i < types.size(); i++) {
                    if (s.startsWith(types.get(i))) {
                        return i;
                    }
                }
                return -1; // 默认返回-1
            }
        });

        // 输出排序后的结果
        for (Question q : ans) {
            System.out.println(q.getFrontendQuestionId());
        }
    }
}
