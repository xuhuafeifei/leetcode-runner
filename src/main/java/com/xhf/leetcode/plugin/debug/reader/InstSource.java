package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 指令数据源
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class InstSource {
    /**
     * 阻塞队列, 存储用户输入的命令信息, 并等待消费者消费
     */
    private static final BlockingQueue<String> commandBQ =  new LinkedBlockingQueue<>();

    /**
     * 读取用户输入的命令
     * @param cmd debug cmd
     */
    public static boolean userCmdInput(String cmd) {
        return commandBQ.offer(cmd);
    }

    /**
     * 存储正在消费数据的线程
     */
    private static final Set<Thread> consumerThreadSet = new HashSet<>();

    /**
     * 消费命令
     * @return cmd
     */
    public static String consumeCmd() {
        try {
            synchronized (consumerThreadSet) {
                consumerThreadSet.add(Thread.currentThread());
            }
            return commandBQ.take();
        } catch (InterruptedException e) {
            LogUtils.debug("消费被打断, 终止消费...");
            return "";
        }
    }

    /**
     * 阻塞队列, 存储用户通过UI界面输入的指令信息, 并等待消费者消费
     */
    private static final BlockingQueue<Instrument> uiBQ =  new LinkedBlockingQueue<>();

    /**
     * 存储指令
     * @param instrument
     * @return
     */
    public static boolean uiInstInput(Instrument instrument) {
        return uiBQ.offer(instrument);
    }

    /**
     * 消费指令
     * 不能给方法上锁, 因为take()方法是阻塞的
     * @return
     */
    public static Instrument consumeInst() {
        try {
            synchronized (consumerThreadSet) {
                consumerThreadSet.add(Thread.currentThread());
            }
            return uiBQ.take();
        } catch (InterruptedException e) {
            LogUtils.debug("消费被打断, 终止消费...");
            return null;
        }
    }

    public static void clear() {
        LogUtils.debug("清除数据源中存储的数据...");
        commandBQ.clear();
        uiBQ.clear();
        // 停止所有正在消费的线程, 该方法被env.stop触发. 此时debug结束. 终止一切指令的读取
        synchronized (consumerThreadSet) {
            consumerThreadSet.forEach(Thread::interrupt);
            consumerThreadSet.clear();
        }
    }
}