package com.retry.strategy.annotation;


import com.retry.pojo.AsyncTask;
import com.retry.pojo.RetryInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: hu.chen
 * @Description: 异步调用
 * @DateTime: 2022/7/7 12:57 AM
 **/
@Slf4j
public class AsyncRetryStrategy extends AbstractParentRetryStrategy {

    /**
     * 执行重试
     *
     * @return
     */
    @Override
    public Object execute(RetryInfo retryInfo) throws Throwable {
        long delay = retryInfo.getRetryable().delay();
        // 放进延时队列中
        AsyncTask asyncTask =new AsyncTask(retryInfo,delay);
        AsyncRetryInit.delayQueue.offer(asyncTask);

        return getReturnData(retryInfo.getMethodReturnType());
    }


}
