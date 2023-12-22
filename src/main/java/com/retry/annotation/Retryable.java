package com.retry.annotation;


import java.lang.annotation.*;

/**
 * @Author: hu.chen
 * @Description:
 * @DateTime: 2022/7/6 11:47 PM
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {

    /**
     * 重试次数
     */
    int maxAttempts() default 3;

    /**
     * 重试异常（遇到什么异常进行重试）
     */
    Class<? extends Throwable>[] exceptions() default { Exception.class };

    /**
     * 延迟多长时间进行重试
     */
    long delay() default 3L;

    /**
     * 重试的乘集
     * 比如：delay=3，multiplier=2
     * 第一次重试在3秒后，第二次重试则是3*2=6秒，第三次重拾则是6*2=12秒。。以此类推。。。
     */
    double multiplier() default 1.0D;

    /**
     * 指定兜底方法所在的类，如果不指定则默认查找重试方法所在的类
     *
     * @return
     */
    Class callbackClass() default String.class;

    /**
     * 指定兜底方法的方法名
     *
     * @return
     */
    String callbackMethod() default "";

    /**
     * 是否是同步重试调用，默认是同步重试，false为异步调用
     *
     * @return
     */
    boolean isSync() default true;

}
