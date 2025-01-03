package debug;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {

    public void test() {
        System.out.println(Thread.currentThread().getName());

        // 启动子线程
        Thread notifierThread = new Thread(new Notifier(this));
        notifierThread.start();

        // 主线程阻塞在 Scanner 上
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Main thread waiting for input...");

        try {
            // 主线程阻塞在 readLine 上
            while (!Thread.interrupted()) {  // 检查中断标志
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

        System.out.println("Main thread has stopped waiting.");
    }

    public void stop() {
        System.out.println(Thread.currentThread().getName());
        Thread.yield();  // fetch output
        // 中断主线程
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
                // 模拟子线程等待 2 秒
                Thread.sleep(2000);
                System.out.println("Notifier thread interrupting main thread.");
                main.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

