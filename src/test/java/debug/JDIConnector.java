package debug;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.EventRequestManager;

import java.util.Map;

public class JDIConnector {
    public static void main(String[] args) throws Exception {
        // 1. 获取连接参数
        Map<String, Connector.Argument> connectorArgs = ArgumentsBuilder.buildArguments("localhost", 5005);

        // 2. 获取 SocketAttachingConnector 实例
//            SocketAttachingConnector connector = (SocketAttachingConnector)
        AttachingConnector connector = (AttachingConnector) Bootstrap.virtualMachineManager().allConnectors().stream()
                .filter(c -> "com.sun.jdi.SocketAttach".equals(c.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SocketAttach connector not found"));

        // 3. 使用 SocketAttachingConnector 连接远程JVM
        VirtualMachine vm = connector.attach(connectorArgs);

        // 4. 获取事件请求管理器
        EventRequestManager erm = vm.eventRequestManager();

        // 5. 设置断点
//        ReferenceType referenceType = vm.classesByName("Solution").get(0); // 找到 Solution 类
//        Location location = referenceType.locationsOfLine(14).get(0);  // 你想调试的行号，假设在第14行
//        BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
//        breakpointRequest.enable();

        // 6. 启用调试模式
        vm.setDebugTraceMode(VirtualMachine.TRACE_NONE);

        // 7. 监听事件并处理断点事件
        while (true) {
            EventSet eventSet = vm.eventQueue().remove();
            for (Event event : eventSet) {
                if (event instanceof BreakpointEvent) {
                    BreakpointEvent bpEvent = (BreakpointEvent) event;
                    System.out.println("Hit breakpoint at: " + bpEvent.location());
                    // 你可以在这里打印更多变量或堆栈信息
                }
            }
            eventSet.resume(); // 继续执行程序
        }

    }
}

class ArgumentsBuilder {
    public static Map<String, Connector.Argument> buildArguments(String host, int port) {
        // 获取 SocketAttach 连接器的所有连接参数
        Connector connector = Bootstrap.virtualMachineManager().allConnectors().stream()
                .filter(c -> "com.sun.jdi.SocketAttach".equals(c.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SocketAttach connector not found"));

        // 创建连接参数
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue(host);
        arguments.get("port").setValue(String.valueOf(port));

        return arguments;
    }
}
