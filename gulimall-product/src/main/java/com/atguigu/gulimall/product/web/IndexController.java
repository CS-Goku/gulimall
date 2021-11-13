package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
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

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //1、查询所有一级分类
        List<CategoryEntity> level1s = categoryService.selectLevel1();
        model.addAttribute("level1s",level1s);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson(){

        //查询2级分类,2级中包含3级分类集合，用map封装每一个2分类，key是1级分类的id
        Map<String, List<Catelog2Vo>> level2s = categoryService.selectLevel2();

        return level2s;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
