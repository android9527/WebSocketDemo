package org.websocket.demo.util;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenfeiyue on 15/7/6.
 */
public class AsyncTaskExecutors {

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };

    /**
     * An {@link java.util.concurrent.Executor} that can be used to execute tasks in parallel.
     */

//    private static final MoreExecutor MORE_EXECUTOR = new MoreExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
//            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


    private static MoreExecutor THREAD_POOL_EXECUTOR = new MoreExecutor(0, Integer.MAX_VALUE,
            1L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());


    /**
     * 并发执行多任务
     *
     * @param runnable runnable
     * @return
     */
    public static Future<?> executeTask(final Runnable runnable) {

        if (THREAD_POOL_EXECUTOR.isShutdown()) {
            THREAD_POOL_EXECUTOR = new MoreExecutor(0, Integer.MAX_VALUE,
                    1L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        return THREAD_POOL_EXECUTOR.executeTask(runnable);
    }


    /**
     * 取消单个任务
     * Thread.currentThread().isInterrupted()
     *
     * @param future
     * @param mayInterruptIfRunning
     */
    public static void cancelTask(Future<?> future, boolean mayInterruptIfRunning) {
        future.cancel(mayInterruptIfRunning);
    }


    public static void cancelTask(Future<?> future) {
        cancelTask(future, true);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     * <p/>
     * <p>This method does not wait for previously submitted tasks to
     * complete execution.  Use {@link #awaitTermination awaitTermination}
     * to do that.
     */
    public static void shutdown() {
        THREAD_POOL_EXECUTOR.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks
     * that were awaiting execution. These tasks are drained (removed)
     * from the task queue upon return from this method.
     * <p/>
     * <p>This method does not wait for actively executing tasks to
     * terminate.  Use {@link #awaitTermination awaitTermination} to
     * do that.
     * <p/>
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  This implementation
     * cancels tasks via {@link Thread#interrupt}, so any task that
     * fails to respond to interrupts may never terminate.
     */
    public static List<Runnable> shutdownNow() {
        return THREAD_POOL_EXECUTOR.shutdownNow();
    }


    private static class MoreExecutor extends ThreadPoolExecutor {
        public static int runCount = 0;

        public MoreExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        public MoreExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        public MoreExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        private void editRuncount(boolean add) {

        }

        public synchronized Future<?> executeTask(final Runnable runnable) {
            editRuncount(true);
            return submit(runnable);
        }
    }

}
