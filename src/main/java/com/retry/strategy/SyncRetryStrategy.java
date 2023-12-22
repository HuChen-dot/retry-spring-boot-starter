package com.retry.strategy;


import com.retry.pojo.RetryInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: hu.chen
 * @Description: 同步调用
 * @DateTime: 2022/7/7 12:57 AM
 **/
@Slf4j
public class SyncRetryStrategy extends AbstractParentRetryStrategy {


    /**
     * 同步执行重试
     *
     * @return
     */
    @Override
    public Object execute(RetryInfo retryInfo) throws Throwable {
        Object result = null;
        // 重试次数
        int count = retryInfo.getRetryable().maxAttempts();
        // 初始次数
        int initCount = 1;
        // 延迟多长时间进行重试
        long delay = retryInfo.getRetryable().delay();
        boolean flag = false;
        while (count >= initCount) {
            try {
                // 休眠指定时间
                TimeUnit.SECONDS.sleep(delay);
                // 执行
                result = run(retryInfo.getClassName(), retryInfo.getMethodName(), retryInfo.getArgs());
                flag = true;
                initCount += count;
            } catch (Throwable ex) {
                log.error("第 " + initCount + " 次重试异常：", ex);
                initCount++;
                double multiplier = retryInfo.getRetryable().multiplier();
                // 计算延迟时间
                delay = (long) (delay * multiplier);
            }
        }
        if (!flag) {
            // 执行兜底逻辑
            result = callBack(retryInfo);
        }
        if (result == null) {
            result = getReturnData(retryInfo.getMethodReturnType());
        }
        return result;
    }


}
