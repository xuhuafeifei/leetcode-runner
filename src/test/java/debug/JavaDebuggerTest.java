package debug;

import org.junit.Test;

import java.util.HashMap;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebuggerTest {
    @Test
    public void test() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 100; ++i) {
            map.put(i, i);
        }
        System.out.println(map);
    }

    public static void main(String[] args) {
//        t1();
//        t2();
    }

//    public static void t1(){
//        JavaDebugger javaDebugger = new JavaDebugger(null, new JavaDebugConfig(new StdInReader(), new StdOutput()));
//        javaDebugger.start();
//    }

//    public static void t2(){
//
//        String res;
//        InstReader reader = new StdInReader(null);
//        while (true) {
//            res = reader.readInst();
//            if (res.equals("bk") || res.equals("exit")) {
//                return;
//            }
//            // 解析指令, 并执行
//            Instruction parse = new InstParserImpl().parse(res);
//            // debug parse
//            if (parse == null) {
//                LogUtils.simpleDebug("指令解析失败");
//                continue;
//            }
//            LogUtils.simpleDebug(parse.toString());
//            JavaInstFactory instance = JavaInstFactory.getInstance();
//            InstExecutor instExecutor = instance.create(parse);
//            // debug
//            LogUtils.simpleDebug(instExecutor.getClass().getName());
//        }
//    }

}
