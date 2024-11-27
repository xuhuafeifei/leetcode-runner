package eventbus;

import com.xhf.leetcode.plugin.bus.LCEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.bus.LoginEvent;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class EventBusDemo {
    static LCEventBus bus = LCEventBus.getInstance();

    static class Subscriber implements LCSubscriber {
        @Override
        public void onEvent(LCEvent event) {
            System.out.println("Subscriber: " + event);
        }
    }

    @Test
    public void testLCEventBus() throws InterruptedException {
        Subscriber subscriber = new Subscriber();
        Subscriber subscriber1 = new Subscriber();
//        bus.register("login", subscriber);
//        bus.register("login2", subscriber1);
//        bus.register("login2", subscriber);

//        bus.post(new LoginEvent("login"));
        Thread.sleep(1000);
//        bus.post(new LoginEvent("login2"));
    }
}
