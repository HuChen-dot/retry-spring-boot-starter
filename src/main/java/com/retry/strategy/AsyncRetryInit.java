package com.retry.strategy;

import com.retry.pojo.AsyncTask;
import com.retry.pojo.RetryInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenhu
 */
@Component
@Slf4j
public class AsyncRetryInit extends AbstractParentRetryStrategy {


    public static DelayQueue<AsyncTask> delayQueue = new DelayQueue<AsyncTask>();


    private Executor executor = new ThreadPoolExecutor(10,30,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100),new DefaultThreadFactory());

    @PostConstruct
    public void run() {


        new Thread(() -> {
            while (true) {
                try {
                    AsyncTask take = delayQueue.take();

                    // 将任务放进线程池中执行
                    executor.execute(()->{
                        AtomicInteger retryCount = take.getRetryCount();
                        int i = retryCount.addAndGet(1);

                        try {
                            // 执行原始方法
                            run(take.getRetryInfo().getClassName(),take.getRetryInfo().getMethodName(),take.getRetryInfo().getArgs());
                        } catch (Exception e) {
                            log.error("第 " + i + " 次重试异常：", e);

                            if( i>=take.getRetryInfo().getRetryable().maxAttempts()){
                                try {
                                    callBack(take.getRetryInfo());
                                } catch (Throwable e1) {
                                    log.error("兜底方法执行失败",e1);
                                }
                            }else {
                                double multiplier = take.getRetryInfo().getRetryable().multiplier();
                                // 计算延迟时间
                                long delay = (long) (take.getRetryInfo().getRetryable().delay() * multiplier);

                                // 放进延时队列中
                                AsyncTask asyncTask =new AsyncTask(take.getRetryInfo(),delay);
                                asyncTask.setRetryCount(new AtomicInteger(i));
                                // 再次放入延时队列中等待执行
                                AsyncRetryInit.delayQueue.offer(asyncTask);
                            }

                        }


                    });

                } catch (Exception e) {
                    log.error("annotation.AsyncRetryInit获取任务失败", e);
                }


            }


        }).start();


    }

    @Override
    public Object execute(RetryInfo retryInfo) throws Throwable {
        return null;
    }


    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "操作异步重试线程池" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
