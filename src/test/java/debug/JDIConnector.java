package debug;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.IOException;
import java.util.Map;

public class JDIConnector {
    public static void main(String[] args) throws Exception {
        // 1. ��ȡ���Ӳ���
        Map<String, Connector.Argument> connectorArgs = ArgumentsBuilder.buildArguments("localhost", 5005);

        // 2. ��ȡ SocketAttachingConnector ʵ��
//            SocketAttachingConnector connector = (SocketAttachingConnector)
        AttachingConnector connector = (AttachingConnector) Bootstrap.virtualMachineManager().allConnectors().stream()
                .filter(c -> "com.sun.jdi.SocketAttach".equals(c.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SocketAttach connector not found"));

        // 3. ʹ�� SocketAttachingConnector ����Զ��JVM
        VirtualMachine vm = connector.attach(connectorArgs);

        // 4. ��ȡ�¼����������
        EventRequestManager erm = vm.eventRequestManager();

        // 5. ���öϵ�
//        ReferenceType referenceType = vm.classesByName("Solution").get(0); // �ҵ� Solution ��
//        Location location = referenceType.locationsOfLine(14).get(0);  // ������Ե��кţ������ڵ�14��
//        BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);
//        breakpointRequest.enable();

        // 6. ���õ���ģʽ
        vm.setDebugTraceMode(VirtualMachine.TRACE_NONE);

        // 7. �����¼�������ϵ��¼�
        while (true) {
            EventSet eventSet = vm.eventQueue().remove();
            for (Event event : eventSet) {
                if (event instanceof BreakpointEvent) {
                    BreakpointEvent bpEvent = (BreakpointEvent) event;
                    System.out.println("Hit breakpoint at: " + bpEvent.location());
                    // ������������ӡ����������ջ��Ϣ
                }
            }
            eventSet.resume(); // ����ִ�г���
        }

    }
}

class ArgumentsBuilder {
    public static Map<String, Connector.Argument> buildArguments(String host, int port) {
        // ��ȡ SocketAttach ���������������Ӳ���
        Connector connector = Bootstrap.virtualMachineManager().allConnectors().stream()
                .filter(c -> "com.sun.jdi.SocketAttach".equals(c.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SocketAttach connector not found"));

        // �������Ӳ���
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue(host);
        arguments.get("port").setValue(String.valueOf(port));

        return arguments;
    }
}
