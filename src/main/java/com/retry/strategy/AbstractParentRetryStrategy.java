package com.retry.strategy;


import com.retry.pojo.RetryInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @Author: hu.chen
 * @Description: 执行接口
 * @DateTime: 2022/7/7 12:58 AM
 **/
@Slf4j
public abstract class AbstractParentRetryStrategy implements RetryStrategy {


    /**
     * 执行重试的方法
     *
     * @param retryInfo
     * @return
     * @throws Throwable
     */
    @Override
    public abstract Object execute(RetryInfo retryInfo) throws Throwable;


    /**
     * 执行兜底逻辑
     */
    Object callBack(RetryInfo retryInfo) throws Throwable {
        String callbackMethod = retryInfo.getRetryable().callbackMethod();
        if ("".equals(callbackMethod)) {
            log.error("traceId: " + retryInfo.getTraceId() + " " + retryInfo.getClassName() + "." + retryInfo.getMethodName() + ": 方法重试：{} 次后依然失败，错误是：", retryInfo.getRetryable().maxAttempts(), retryInfo.getE());
            throw retryInfo.getE();
        }

        Class callBackClass = retryInfo.getRetryable().callbackClass();
        Object target = null;
        if (callBackClass.getTypeName().equals(String.class.getTypeName())) {
            //拦截的实体类
            callBackClass = Class.forName(retryInfo.getClassName());
            target = callBackClass.newInstance();
        } else {
            target = callBackClass.newInstance();
        }

        Method[] declaredMethods = callBackClass.getDeclaredMethods();
        Method method = null;
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals(callbackMethod)) {
                method = declaredMethod;
                break;
            }
        }
        if (method == null) {
            log.error("traceId: " + retryInfo.getTraceId() + " "+retryInfo.getClassName() + "." + retryInfo.getMethodName() + ": 设置的回调方法在类中未找到，设置的方法名为：{} ，错误是：", callbackMethod, retryInfo.getE());
            throw retryInfo.getE();
        }
        Object result = null;
        // 开启暴力反射
        method.setAccessible(true);
        int parameterCount = method.getParameterCount();
        Object[] ags = new Object[parameterCount];
        Object[] args = retryInfo.getArgs();
        if (parameterCount != 0) {
            ags[0] = retryInfo.getE();
            for (int i = 0; i < args.length; i++) {
                if (i < (parameterCount - 1)) {
                    ags[i + 1] = args[i];
                }
            }
        }
        try {
            result = method.invoke(target, ags);
        } catch (Throwable e1) {
            log.error("traceId: " + retryInfo.getTraceId() + " "+retryInfo.getClassName() + "." + retryInfo.getMethodName() + ": 兜底方法：{}，执行失败，猜测原因：参数设置错误，兜底方法参数的设置规则为：可以不接收任何参数 或者 有参数但是第一个参数必须为重试的异常或者父异常，其他参数和参数位置顺序和原始方法保持一致并且参数类型不能是基本数据类型，具体堆栈信息：", method.getName(), e1);
            throw e1;
        }
        return result;
    }


    public Object getReturnData(Class<?> returnType) {
        String name = returnType.getName();
        if ("int".equals(name) ||
                "byte".equals(name) ||
                "short".equals(name) ||
                "long".equals(name) ||
                "float".equals(name) ||
                "double".equals(name)) {
            return 0;
        } else if ("char".equals(name)) {
            return '0';
        } else if ("boolean".equals(name)) {
            return false;
        } else {
            return null;
        }
    }


    public static Object run(String className, String methodName, Object[] args) throws Exception {
        Class<?> aClass = Class.forName(className);
        Object o = aClass.newInstance();
        Method[] methods = aClass.getMethods();
        Method m = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                m = method;
                break;
            }
        }
        m.setAccessible(true);
       return m.invoke(o, args);
    }

}
