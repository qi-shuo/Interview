package com.qishuo.interview.threadlocal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 使用缓存优化InheritableTask
 *
 * @author QiShuo
 * @version 1.0
 * @create 2019-12-03 14:27
 */
public abstract class InheritableTaskWithCache implements Runnable {
    private Object threadLocalsMapObj;
    private static volatile Field inheritableThreadLocalsField;
    private static volatile Class threadLocalMapClazz;
    private static volatile Method createInheritedMapMethod;
    private static final Object accessLock = new Object();

    public InheritableTaskWithCache() {
        try {
            Thread currentThread = Thread.currentThread();
            Field field = getInheritableThreadLocalsField();
            //得到当前线程的inheritableThreadLocals的值ThreadLocalMap
            Object threadLocalsMapObj = field.get(currentThread);
            if (null != threadLocalsMapObj) {
                Class threadLocalMapClazz = getThreadLocalMapClazz();
                Method method = getCreateInheritedMapMethod(threadLocalMapClazz);
                //创建一个新的ThreadLocalMap
                this.threadLocalsMapObj = method.invoke(ThreadLocal.class, threadLocalsMapObj);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Field getInheritableThreadLocalsField() {
        if (null == inheritableThreadLocalsField) {
            synchronized (accessLock) {
                if (null == inheritableThreadLocalsField) {
                    try {
                        Field field = Thread.class.getDeclaredField("inheritableThreadLocals");
                        field.setAccessible(true);
                        inheritableThreadLocalsField = field;
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return inheritableThreadLocalsField;
    }

    private Method getCreateInheritedMapMethod(Class threadLocalMapClazz) {
        if (null != threadLocalMapClazz && null == createInheritedMapMethod) {
            synchronized (accessLock) {
                if (null == createInheritedMapMethod) {
                    try {
                        Method method = ThreadLocal.class.getDeclaredMethod("createInheritedMap", threadLocalMapClazz);
                        method.setAccessible(true);
                        createInheritedMapMethod = method;
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return createInheritedMapMethod;
    }

    private Class getThreadLocalMapClazz() {
        if (null == inheritableThreadLocalsField) {
            return null;
        }
        if (null == threadLocalMapClazz) {
            synchronized (accessLock) {
                if (null == threadLocalMapClazz) {
                    threadLocalMapClazz = inheritableThreadLocalsField.getType();
                }
            }
        }
        return threadLocalMapClazz;
    }

    /**
     * 代理方法这个方法处理业务逻辑
     */
    protected abstract void runTask();

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        Field field = getInheritableThreadLocalsField();
        try {
            if (null != threadLocalsMapObj && null != field) {
                field.set(currentThread, threadLocalsMapObj);
                threadLocalsMapObj = null;
            }
            runTask();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if (field != null) {
                    field.set(currentThread, null);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
}
