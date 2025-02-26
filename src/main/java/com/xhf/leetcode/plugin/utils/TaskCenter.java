package com.xhf.leetcode.plugin.utils;

import com.intellij.openapi.application.ApplicationManager;

import java.util.concurrent.*;

/**
 * 任务执行中心
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TaskCenter {
    // 创建线程池
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
    // 单例
    private static volatile TaskCenter instance;
    private TaskCenter() {

    }
    public static TaskCenter getInstance() {
        if (instance == null) {
            synchronized (TaskCenter.class) {
                if (instance == null) {
                    instance = new TaskCenter();
                }
            }
        }
        return instance;
    }


    public interface Task<T> {
        /**
         * 异步运行task
         */
        void invokeLater();

        /**
         * 运行并等待task执行完成
         */

        void invokeAndWait();

        /**
         * 运行task, 并获取task的返回值
         * @return T
         */

        T invokeAndGet();
    }

    /**
     * 抽象Task, 内部负责运行runnable, 没有返回值
     */
    public abstract static class AbstractTask implements Task<Void> {
        protected final Runnable runnable;

        public AbstractTask(Runnable runnable) {
            this.runnable = runnable;
        }
    }


    /**
     * 抽象FutureTask, 内部负责运行runnable, 有返回值
     */
    public abstract static class AbstractFutureTask<T> implements Task<T> {
        protected final Callable<T> callback;

        public AbstractFutureTask(Callable<T> callback) {
            this.callback = callback;
        }
    }

    /**
     * 同步Task, 串行执行runnable
     */
    public static class SyncTask extends AbstractTask {

        public SyncTask(Runnable runnable) {
            super(runnable);
        }

        @Override
        public void invokeLater() {
            throw new RuntimeException("SyncTask is not support invokeLater");
        }

        @Override
        public void invokeAndWait() {
            runnable.run();
        }

        @Override
        public Void invokeAndGet() {
            runnable.run();
            return null;
        }
    }

    /**
     * 异步Task, 异步执行task. 内部存在两种异步方式, 其一是由线程池运行, 其二是单独创建全新的线程, 由新线程运行
     */
    public class AsyncTask extends AbstractTask {

        private final boolean inThreadPool;

        public AsyncTask(Runnable runnable, boolean inThreadPool) {
            super(runnable);
            this.inThreadPool = inThreadPool;
        }

        public AsyncTask(Runnable runnable) {
            this(runnable, true);
        }

        @Override
        public void invokeLater() {
            if (inThreadPool) {
                threadPoolExecutor.execute(runnable);
            } else {
                new Thread(runnable).start();
            }
        }

        @Override
        public void invokeAndWait() {
            FutureTask<Void> voidFutureTask = new FutureTask<Void>(runnable, null);
            if (inThreadPool) {
                execute(voidFutureTask);
                try {
                    voidFutureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                new Thread(voidFutureTask).start();
                try {
                    voidFutureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public Void invokeAndGet() {
            runnable.run();
            return null;
        }
    }

    /**
     * 同步的未来任务对象, 串行执行Task任务
     * @param <T>
     */
    public static class SyncFutureTask<T> extends AbstractFutureTask<T> {

        public SyncFutureTask(Callable<T> callback) {
            super(callback);
        }

        @Override
        public void invokeLater() {
            throw new RuntimeException("Sync FutureTask is not support invokeLater");
        }

        @Override
        public void invokeAndWait() {
            try {
                callback.call();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
        }

        @Override
        public T invokeAndGet() {
            try {
                return callback.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 异步的未来任务对象, 异步执行Task任务
     * @param <T>
     */
    public class AsyncFutureTask<T> extends AbstractFutureTask<T> {

        private final boolean inThreadPool;

        public AsyncFutureTask(Callable<T> callback, boolean inThreadPool) {
            super(callback);
            this.inThreadPool = inThreadPool;
        }

        public AsyncFutureTask(Callable<T> callback) {
            this(callback, true);
        }

        @Override
        public void invokeLater() {
            if (inThreadPool) {
                threadPoolExecutor.execute(new FutureTask<>(callback));
            } else {
                new Thread(() -> {
                    try {
                        callback.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }

        @Override
        public void invokeAndWait() {
            FutureTask<T> futureTask = new FutureTask<>(callback);
            if (inThreadPool) {
                execute(futureTask);
                try {
                    futureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                new Thread(futureTask).start();
                try {
                    futureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public T invokeAndGet() {
            FutureTask<T> futureTask = new FutureTask<>(callback);
            if (inThreadPool) {
                execute(futureTask);
                try {
                    return futureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                new Thread(futureTask).start();
                try {
                    return futureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 在EDT线程中运行的task
     */
    public static class EDTTask extends AbstractTask {

        public EDTTask(Runnable runnable) {
            super(runnable);
        }

        @Override
        public void invokeLater() {
            ApplicationManager.getApplication().invokeLater(runnable);
        }

        @Override
        public void invokeAndWait() {
            ApplicationManager.getApplication().invokeAndWait(runnable);
        }

        @Override
        public Void invokeAndGet() {
            ApplicationManager.getApplication().invokeAndWait(runnable);
            return null;
        }
    }

    public static class EDTFutureTask<T> extends AbstractFutureTask<T> {

        public EDTFutureTask(Callable<T> callback) {
            super(callback);
        }

        @Override
        public void invokeLater() {
            ApplicationManager.getApplication().invokeLater(new FutureTask<>(callback));
        }

        @Override
        public void invokeAndWait() {
            FutureTask<T> futureTask = new FutureTask<>(callback);
            ApplicationManager.getApplication().invokeLater(futureTask);
            try {
                futureTask.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public T invokeAndGet() {
            FutureTask<T> futureTask = new FutureTask<>(callback);
            ApplicationManager.getApplication().invokeLater(futureTask);
            try {
                return futureTask.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void execute(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    /**
     * 创建异步task任务, 并且任务将被提交到线程池
     * @param runnable runnable
     * @return Task
     */
    public Task<Void> createTask(Runnable runnable) {
        return createTask(runnable, true);
    }

    public Task<Void> createTask(Runnable runnable, boolean async) {
        return createTask(runnable, async, true);
    }

    public Task<Void> createTask(Runnable runnable, boolean async, boolean inThreadPool) {
        if (async) {
            return new AsyncTask(runnable, inThreadPool);
        } else {
            return new SyncTask(runnable);
        }
    }


    /**
     * 创建异步Future Task, 并且任务将被提交到线程池
     * @param callback callback
     * @return Task
     * @param <T> T
     */
    public <T> Task<T> createFutureTask(Callable<T> callback) {
        return createFutureTask(callback, true);
    }

    private <T> Task<T> createFutureTask(Callable<T> callback, boolean async) {
        return createFutureTask(callback, async, true);
    }

    public <T> Task<T> createFutureTask(Callable<T> callback, boolean async, boolean inThreadPool) {
        if (async) {
            return new AsyncFutureTask<>(callback, inThreadPool);
        } else {
            return new SyncFutureTask<>(callback);
        }
    }

    /**
     * 创建EDT Task, 并在EDT线程中执行
     * @param runnable runnable
     * @return Task
     */
    public Task<Void> createEDTTask(Runnable runnable) {
        return new EDTTask(runnable);
    }

    /**
     * 创建EDT Task, 并在EDT线程中执行, 返回对象支持获取数据
     * @param callback callback
     * @return T
     * @param <T> T
     */
    public <T> Task<T> createEDTFutureTask(Callable<T> callback) {
        return new EDTFutureTask<>(callback);
    }
}
