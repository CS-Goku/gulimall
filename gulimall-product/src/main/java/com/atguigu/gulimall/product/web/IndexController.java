package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        //1、查询所有一级分类
        List<CategoryEntity> level1s = categoryService.selectLevel1();
        model.addAttribute("level1s", level1s);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException {

        //查询2级分类,2级中包含3级分类集合，用map封装每一个2分类，key是1级分类的id
        Map<String, List<Catelog2Vo>> level2s = categoryService.selectLevel2();

        return level2s;
    }



    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        RLock lock = redissonClient.getLock("my_lock");
        try {
            //1、加锁，阻塞式等待，得不到锁一直想拿锁，业务没执行完，自动给锁续期+30s，不用担心业务超时锁过期
            //2、业务执行完，不释放锁也会，自动过期
            lock.lock(30, TimeUnit.SECONDS);//自己设置时间不会自动续期
            System.out.println("加锁：" + Thread.currentThread().getId());
            Thread.sleep(1000);//模拟业务执行时间
        }catch (Exception e){

        }finally {
            //业务没运行完，也会释放锁
            System.out.println("释放锁："+ Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }


    //保证一定能读到最新数据，修改期间写锁是一个排他锁（互斥锁），读锁是一个共享锁；写锁没释放，读就必须等待
    //读+写：等待读释放
    //写+写：阻塞，互斥
    //写+读：等待写释放
    //读+读：效果等于无锁，并发读
    //总结：只要有写的存在，都要等待
    @ResponseBody
    @GetMapping("/read")
    public String readValue(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.readLock();
        try {
            rLock.lock();
            s = redisTemplate.opsForValue().get("writeValue");
            System.out.println("加读锁"+Thread.currentThread().getId());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;


    }


    @ResponseBody
    @GetMapping("/write")
    public String writeValue(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue",s);
            System.out.println("加写锁"+Thread.currentThread().getId());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }

        return s;
    }
}
