package com.retry.pojo;

import com.retry.annotation.Retryable;
import lombok.Data;
/**
 * @author chenhu
 */
@Data
public class RetryInfo {
    /**
     * 需要重试的方法的类名
     */
    private String className;

    /**
     * 需要重试的方法名
     */
    private String methodName;

    /**
     * 方法返回值类型
     */
    private  Class<?> methodReturnType;

    /**
     * 需要重试方法的参数
     */
    private Object[] args;

    /**
     * 发生的异常
     */
    private Throwable e;

    /**
     * 注解参数
     */
    private Retryable retryable;

    /**
     * 链路id
     */
    private String traceId;




}
