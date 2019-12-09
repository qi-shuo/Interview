package com.qishuo.interview.threadlocal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 解决InheritableThreadLocal遇见线程池问题
 *
 * @author QiShuo
 * @version 1.0
 * @create 2019-12-03 11:48
 */
public abstract class InheritableTask implements Runnable {
    private Object inheritableThreadLocalsObj;

    public InheritableTask() {
        try {
            //获取当前业务线程
            Thread currentThread = Thread.currentThread();
            //获取inheritableThreadLocals属性值
            Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocalsField.setAccessible(true);
            //得到当前线程inheritableThreadLocals的属性值
            Object threadLocalMapObj = inheritableThreadLocalsField.get(currentThread);
            if (null != threadLocalMapObj) {
                //获取字段的类型
                Class<?> threadLocalMapClazz = inheritableThreadLocalsField.getType();
                //获取ThreadLocal中的createInheritedMap方法
                Method method = ThreadLocal.class.getDeclaredMethod("createInheritedMap", threadLocalMapClazz);
                method.setAccessible(true);
                //调用createInheritedMap方法，重新创建一个新的inheritableThreadLocals，并且将这个值保存
                this.inheritableThreadLocalsObj = method.invoke(ThreadLocal.class, threadLocalMapObj);
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void run() {
        //此处获取处理当前业务的线程，也就是线程池中的线程
        Thread currentThread = Thread.currentThread();
        Field field = null;
        try {
            //获取inheritableThreadLocals属性
            field = Thread.class.getDeclaredField("inheritableThreadLocals");
            //设置权限
            field.setAccessible(true);
            if (this.inheritableThreadLocalsObj != null) {
                //将暂存值，赋值给currentThread
                field.set(currentThread, this.inheritableThreadLocalsObj);
                inheritableThreadLocalsObj = null;
            }
            //执行任务
            runTask();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                //最后将线程的InheritableThreadLocals设置为null
                if (field != null) {
                    field.set(currentThread, null);
                }
            } catch (IllegalAccessException e) {
                System.out.println(e.toString());
            }
        }
    }

    /**
     * 代理方法这个方法处理业务逻辑
     */
    protected abstract void runTask();


}
