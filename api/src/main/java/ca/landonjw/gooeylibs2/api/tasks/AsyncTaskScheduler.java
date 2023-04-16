package ca.landonjw.gooeylibs2.api.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class AsyncTaskScheduler {
    private static final ScheduledThreadPoolExecutor SCHEDULER = new ScheduledThreadPoolExecutor(4, r -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("GooeyLibs Async Scheduler");
        return thread;
    });
    private static final ForkJoinPool WORKER = new ForkJoinPool(16, new WorkerThreadFactory(), new ExceptionHandler(), false);

    public static ScheduledTask schedule(AbstractTask task) {
        if (!task.isAsync()) {
            throw new IllegalArgumentException("Task is not an async task!");
        }
        ScheduledFuture<?> future = SCHEDULER.scheduleAtFixedRate(() -> WORKER.execute(task::tick), task.delay(), task.delay(), TimeUnit.SECONDS);
        return () -> future.cancel(false);
    }

    static {
        SCHEDULER.setRemoveOnCancelPolicy(true);
        SCHEDULER.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    private static final class WorkerThreadFactory
            implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AtomicInteger COUNT = new AtomicInteger(0);

        private WorkerThreadFactory() {
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setDaemon(true);
            thread.setName("GooeyLibs Async Worker: " + COUNT.getAndIncrement());
            thread.setContextClassLoader(AsyncTaskScheduler.class.getClassLoader());
            return thread;
        }
    }

    private static final class ExceptionHandler
            implements Thread.UncaughtExceptionHandler {
        private ExceptionHandler() {
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Logger logger = LogManager.getLogger("GooeyLibs");
            logger.warn(MarkerManager.getMarker("Scheduling"), "Thread " + t.getName() + " threw an uncaught exception", e);
        }
    }
}