package com.retry.strategy.annotation;


import com.retry.pojo.RetryInfo;


/**
 * @Author: hu.chen
 * @Description: 执行接口
 * @DateTime: 2022/7/7 12:58 AM
 **/

public interface RetryStrategy {

    /**
     * 执行重试
     * @param retryInfo
     * @return
     * @throws Throwable
     */
    Object execute(RetryInfo retryInfo) throws Throwable;

}
