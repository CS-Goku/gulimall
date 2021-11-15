package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

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
            lock.lock();
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
}
