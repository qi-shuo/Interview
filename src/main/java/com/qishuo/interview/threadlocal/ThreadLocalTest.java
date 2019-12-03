package com.qishuo.interview.threadlocal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author QiShuo
 * @version 1.0
 * @create 2019-12-03 13:55
 */
public class ThreadLocalTest {
    public static void main(String[] args) throws InterruptedException {
        InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        inheritableThreadLocal.set("qis");
        System.out.println(inheritableThreadLocal.get());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            System.out.println(inheritableThreadLocal.get());
            inheritableThreadLocal.set("qishuo");
            System.out.println(inheritableThreadLocal.get());
        });
        Thread.sleep(1000);
        System.out.println("----------------");
        executorService.execute(() -> {
            System.out.println(inheritableThreadLocal.get());
        });
        Thread.sleep(1000);
        System.out.println("--------------分隔新以上是没有使用InheritableTask----------------");

        executorService.submit(new InheritableTaskWithCache() {
            @Override
            public void runTask() {
                System.out.println(inheritableThreadLocal.get());
                inheritableThreadLocal.set("qishuo");
                System.out.println(inheritableThreadLocal.get());
            }
        });
        Thread.sleep(1000);
        System.out.println("----------------");
        executorService.submit(new InheritableTaskWithCache() {
            @Override
            public void runTask() {
                System.out.println(inheritableThreadLocal.get());
            }
        });
    }
}
