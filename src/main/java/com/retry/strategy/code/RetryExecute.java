//package com.retry.strategy.code;
//
//import lombok.extern.slf4j.Slf4j;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Function;
//
//
//@Slf4j
//public class RetryExecute {
//
//
//    /**
//     * 同步执行
//     *
//     * @param run            需要保护的方法
//     * @param args           参数
//     * @param maxAttempts    重试次数
//     * @param delay          重试间隔时间
//     * @param callbackClass  指定兜底方法所在的类，如果不指定则默认查找重试方法所在的类
//     * @param callbackMethod 指定兜底方法的方法名
//     * @return
//     */
//    public static Object sync(Function<Object[], Object> run, int maxAttempts, long delay, Class callbackClass, String callbackMethod, Object... args) {
//        Object result = null;
//        // 重试次数
//        int count = maxAttempts;
//        // 初始次数
//        int initCount = 1;
//        boolean flag = false;
//        Throwable e = null;
//        while (count >= initCount) {
//            try {
//                // 休眠指定时间
//                TimeUnit.SECONDS.sleep(delay);
//                // 执行
//                result = run.apply(args);
//                flag = true;
//                initCount += count;
//            } catch (Throwable ex) {
//                log.error("第 " + initCount + " 次重试异常：", ex);
//                e = ex;
//                initCount++;
//            }
//        }
//        if (!flag) {
//            try {
//                // 执行兜底逻辑
//                result = callBack(maxAttempts, e, callbackClass, callbackMethod, args);
//            } catch (Throwable e1) {
//                log.error("兜底方法执行失败", e1);
//            }
//        }
//        if (result == null) {
//            result = getReturnData(result.getClass());
//        }
//        return result;
//
//    }
//
//
//    /**
//     * 执行兜底逻辑
//     */
//   private static Object callBack(int maxAttempts, Throwable e, Class callbackClass, String callbackMethod, Object... args1) throws Throwable {
//
//        if (callbackClass == null || "".equals(callbackMethod)) {
//            log.error("方法重试：{} 次后依然失败，错误是：", maxAttempts, e);
//            throw e;
//        }
//
//
//        Object target = callbackClass.newInstance();
//
//
//        Method[] declaredMethods = callbackClass.getDeclaredMethods();
//        Method method = null;
//        for (Method declaredMethod : declaredMethods) {
//            if (declaredMethod.getName().equals(callbackMethod)) {
//                method = declaredMethod;
//                break;
//            }
//        }
//        if (method == null) {
//            log.error("设置的回调方法在类中未找到，设置的方法名为：{} ，错误是：", callbackMethod, e);
//            throw e;
//        }
//        Object result = null;
//        // 开启暴力反射
//        method.setAccessible(true);
//        int parameterCount = method.getParameterCount();
//        Object[] ags = new Object[parameterCount];
//        Object[] args = args1;
//        if (parameterCount != 0) {
//            ags[0] = e;
//            for (int i = 0; i < args.length; i++) {
//                if (i < (parameterCount - 1)) {
//                    ags[i + 1] = args[i];
//                }
//            }
//        }
//        try {
//            result = method.invoke(target, ags);
//        } catch (Throwable e1) {
//            log.error("兜底方法：{}，执行失败，猜测原因：参数设置错误，兜底方法参数的设置规则为：可以不接收任何参数 或者 有参数但是第一个参数必须为重试的异常或者父异常，其他参数和参数位置顺序和原始方法保持一致并且参数类型不能是基本数据类型，具体堆栈信息：", method.getName(), e1);
//            throw e1;
//        }
//        return result;
//    }
//
//
//    public static Object getReturnData(Class r) {
//        String name = r.getName();
//        if ("int".equals(name) ||
//                "byte".equals(name) ||
//                "short".equals(name) ||
//                "long".equals(name) ||
//                "float".equals(name) ||
//                "double".equals(name)) {
//            return 0;
//        } else if ("char".equals(name)) {
//            return '0';
//        } else if ("boolean".equals(name)) {
//            return false;
//        } else {
//            return null;
//        }
//    }
//
//}
