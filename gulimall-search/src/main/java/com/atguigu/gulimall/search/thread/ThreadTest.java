package com.atguigu.gulimall.search.thread;

import org.springframework.cache.annotation.Cacheable;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main方法开始");

        //1、继承Thread类
        Thread01 thread01 = new Thread01();
        thread01.start();

        //2、实现Runnable接口
        new Thread(new Runnable01()).start();

        //3、实现Callable接口,有返回值
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();
        Integer i = futureTask.get();//等待线程执行完毕获取到这个值

        /**
         * 4、线程池：Executors.newFixedThreadPool(10);
         *
         * 好处：
         * 1）降低资源消耗；2）提高响应速度；3）提高线程的可管理性。
         *
         * 七大参数
         * in corePoolSize 核心线程数；线程池创建好以后就准备就绪的线程数量，等待接受异步任务执行
         * int maximumPoolSize 最大线程数量；控制资源
         * long keepAliveTime 存活事件。如果当前的线程数量大于corePoolSize数量，释放空闲的线程（超出核心线程数量的部分）条件：超出核心线程数量的线程空闲时间超过long keepAliveTime就会被释放
         * TimeUnit unit 时间单位
         * BlockingQueue<Runnable> workQueue 阻塞队列，只要有空闲线程就会来这里取出任务去执行
         * ThreadFactory threadFactory 线程的创建工厂
         * RejectedExecutionHandler handler 如果队列满了，按照我们指定的拒绝策略拒绝执行任务
         *
         * 运行流程：
         * 1、线程池创建，准备核心线程接收任务
         * 2、核心满了去阻塞队列
         * 3、阻塞队列满了，再开启线程最大开到max个线程
         * 4、空闲线程在keepAliveTime之后自动销毁，最终保持到core数量
         * 5、max满了，剩下的使用Reject指定的拒绝策略
         * 6、所有线程由指定的Factory创建
         */
        service.submit(new Runnable01());//有返回值
        service.execute(new Runnable01());

        System.out.println("main结束" + i);
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("线程启动" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("线程启动" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }

    public static class Callable01 implements Callable {
        @Override
        public Object call() throws Exception {
            System.out.println("线程启动" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }
    }
}
