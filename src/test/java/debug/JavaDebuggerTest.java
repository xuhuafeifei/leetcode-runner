package debug;

import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebuggerTest {
    @Test
    public void test() {

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
//            // ����ָ��, ��ִ��
//            Instruction parse = new InstParserImpl().parse(res);
//            // debug parse
//            if (parse == null) {
//                LogUtils.simpleDebug("ָ�����ʧ��");
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
