package com.retry.pojo;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @author chenhu
 */
@Data
public class AsyncTask implements Delayed {

    private RetryInfo retryInfo;

    /**
     * 已经重试的次数
     */
    private AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * 对象创建时间
     */
    private long start = System.currentTimeMillis() / 1000;

    /**
     * 延时时间（单位秒）
     */
    private long time;


    public AsyncTask(RetryInfo retryInfo, long time) {
        this.retryInfo = retryInfo;
        this.time = time;

    }

    /**
     * 需要实现的接口，获得延迟时间   用（对象创建的固定时间+设定的延迟时间）-当前时间
     *
     * @param unit
     * @return
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert((start + time) - (System.currentTimeMillis() / 1000), TimeUnit.SECONDS);
    }

    /**
     * 用于延迟队列内部比较排序   当前时间的延迟时间 - 比较对象的延迟时间
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Delayed o) {
        AsyncTask o1 = (AsyncTask) o;
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o1.getDelay(TimeUnit.MILLISECONDS));
    }
}
