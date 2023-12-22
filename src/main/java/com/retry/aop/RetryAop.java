package com.retry.aop;


import com.retry.annotation.Retryable;
import com.retry.pojo.RetryInfo;
import com.retry.strategy.annotation.RetryStrategy;
import com.retry.strategy.annotation.AsyncRetryStrategy;
import com.retry.strategy.annotation.SyncRetryStrategy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hu.chen
 * @Description:
 * @DateTime: 2022/7/7 12:01 AM
 **/
@Component
@Aspect
@Slf4j
public class RetryAop {

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.retry.annotation.Retryable)")
    public void annotation() {
    }

    @Around("annotation()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Retryable retryable = getAnnotationLog(point);
        Object[] args = point.getArgs();
        if (retryable == null) {
            return point.proceed(args);
        }
        log.info("开始执行被保护的逻辑，方法参数：{}", args);
        Object result = null;
        try {
            result = point.proceed(args);
        } catch (Throwable e) {
            log.error("任务执行异常：{}", e);

            List<String> clazzs = new ArrayList<>();
            // 获取异常以及该异常的所有父类的全限定名
            getSuperclass(e.getClass(), clazzs);
            boolean flag = false;
            Class<? extends Throwable>[] exceptions = retryable.exceptions();
            for (Class<? extends Throwable> exception : exceptions) {
                if (clazzs.contains(exception.getTypeName())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                log.error("任务执行失败，等待重试.........");
                RetryInfo retryInfo = new RetryInfo();
                retryInfo.setRetryable(retryable);
                retryInfo.setArgs(args);
                retryInfo.setE(e);
                retryInfo.setClassName(point.getSignature().getDeclaringType().getCanonicalName());
                retryInfo.setMethodName(point.getSignature().getName());
                retryInfo.setMethodReturnType(((MethodSignature) point.getSignature()).getMethod().getReturnType());
                return doRetry(retryInfo);
            }
            throw e;
        }
        //返回被增强方法的执行返回值
        return result;
    }

    /**
     * 执行重试
     *
     * @param retryInfo
     * @return
     * @throws Throwable
     */
    private Object doRetry(RetryInfo retryInfo) throws Throwable {
        RetryStrategy retryStrategy = null;
        if (retryInfo.getRetryable().isSync()) {
            // 同步调用
            retryStrategy = new SyncRetryStrategy();
        } else {
            // 异步执行重试
            retryStrategy = new AsyncRetryStrategy();
        }
        // 执行
        return retryStrategy.execute(retryInfo);
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private static Retryable getAnnotationLog(ProceedingJoinPoint point) {
        Signature signature = point.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        if (method != null) {
            return method.getAnnotation(Retryable.class);
        }
        return null;
    }

    /**
     * 递归获取类以及类的父类
     *
     * @param clazz
     * @param clazzs
     */
    private void getSuperclass(Class clazz, List<String> clazzs) {
        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            clazzs.add(superClass.getTypeName());
            getSuperclass(superClass, clazzs);
        }
    }


}
