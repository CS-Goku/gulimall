package com.atguigu.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start...");
        /**
         *1、创建异步对象，runXxx是都没有返回值的，supplyXxx 都是可以获得返回值的
         */
//        CompletableFuture.runAsync(()->{
//            System.out.println("线程启动"+Thread.currentThread().getId());
//            int i = 10 /2;
//            System.out.println("运行结果"+i);
//        },executor);

        /**
         * 2、计算完成时回调方法
         * whenComplete感知结果和异常
         * exceptionally修改返回值
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程启动" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor).whenComplete((res, exception) -> {
//            //1、可以返回结果和异常信息
//            System.out.println("异步任务完成了，结果是：" + res + "。。。。。异常是：" + exception);
//        }).exceptionally(throwable -> {
//            //2、感知到异常返回默认值
//            return 10;
//        });


        /**
         * 3、handle能感知结果和异常以及修改结果
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程启动" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor).handle((res,exception)->{
//            if (res!= null){
//                System.out.println("异常为："+exception);
//                return res*2;
//            }else {
//                System.out.println("异常为："+exception);
//                return 0;
//            }
//        });


        /**
         * 4、线程串行化：
         * thenRunAsync 无结果，无返回值
         * thenAcceptAsync 能接受上一步到结果，无返回值
         * thenApplyAsync 有结果，有返回值
         *
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程启动" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor).thenApplyAsync((res) -> {
//            return "hello" + res;
//        }, executor);


        /**
         * 5、合并两个任务执行之后
         * runAfterBothAsync 没有结果和返回值
         * thenAcceptBothAsync 有前面两个任务的返回值
         * thenCombineAsync 有前两个结果有自身返回值
         *
         */
//        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("任务1结束" + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2启动" + Thread.currentThread().getId());
//            System.out.println("任务2结束");
//            return "hello";
//        }, executor);
//
//        future1.runAfterBothAsync(future2,()->{
//            System.out.println("任务3启动");
//        },executor);
//
//        future1.thenAcceptBothAsync(future2,(f1,f2)->{
//            System.out.println("任务4启动："+f1+":"+f2);
//        },executor);
//
//        CompletableFuture<String> future = future1.thenCombineAsync(future2, (f1, f2) -> {
//            return "任务5" + "->"+f1 +":" +f2;
//        }, executor);
//
//        System.out.println(future.get());


        /**
         * 6、两任务组合-一个完成
         * runAfterEitherAsync：不感知结果，自身无返回值
         * acceptEitherAsync: 感知结果，自身无返回值
         * applyToEitherAsync: 感知结果，自身有返回值
         */
//        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("任务1结束" + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2启动" + Thread.currentThread().getId());
//            System.out.println("任务2结束");
//            return 1;
//        }, executor);

//        future1.runAfterEitherAsync(future2,()->{
//            System.out.println("任务3执行");
//        },executor);

//        future1.acceptEitherAsync(future2,(res)->{
//            System.out.println("任务3执行"+res);
//        },executor);

//        CompletableFuture<String> future = future1.applyToEitherAsync(future2, (res) -> {
//            return "hello" + res;
//        }, executor);
//
//        System.out.println(future.get());


        /**
         * 7、多任务组合
         * allOf:等待所有完成
         * anyOf：只要有一个完成
         */

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询图片信息");
            return "hello.jpg";
        }, executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "黑色+256G";
        }, executor);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍");
            return "华为";
        }, executor);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        allOf.get();//必须全部完成
        System.out.println(futureImg.get()+futureAttr.get()+futureDesc.get());

//        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
//        System.out.println(anyOf.get());
    }
}
