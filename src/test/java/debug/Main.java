package debug;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public void test() {
        System.out.println(Thread.currentThread().getName());

        // �������߳�
        Thread notifierThread = new Thread(new Notifier(this));
        notifierThread.start();

        // ���߳������� Scanner ��
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Main thread waiting for input...");

        try {
            // ���߳������� readLine ��
            while (!Thread.interrupted()) {  // ����жϱ�־
                String ln = in.readLine();
                if (ln == null) {
                    System.out.println("null");
                } else {
                    System.out.println(ln);
                }
            }
        } catch (Exception e) {
            System.out.println("Main thread encountered an error: " + e.getMessage());
        }

        int a = 1;
        Object obj = a;
        boolean b = true;

        System.out.println("Main thread has stopped waiting.");
    }

    public void stop() {
        System.out.println(Thread.currentThread().getName());
        Thread.yield();  // fetch output
        // �ж����߳�
        Thread.currentThread().interrupt();
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.test();
    }

    static class Notifier implements Runnable {

        private final Main main;

        public Notifier(Main main) {
            this.main = main;
            System.out.println("Notifier thread created.");
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName());
                // ģ�����̵߳ȴ� 2 ��
                Thread.sleep(2000);
                System.out.println("Notifier thread interrupting main thread.");
                main.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

