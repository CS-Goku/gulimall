package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类,这个实现类已经继承类服务层的泛型dao，直接调用用baseMapper就好
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->//所有分类用stream的过滤方法，根据条件找到一级分类
                categoryEntity.getParentCid() == 0//一级分类的条件
        ).map(menu -> {//接着用这个一级分类
            menu.setChildren(getChildrens(menu, entities));//菜单的实体类，添加一个private List<CategoryEntity> children;用来装23级分类，获取用到递归方法
            return menu;
        }).sorted((menu1, menu2) -> {//分类排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());//收集


        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        //创建一个集合用来收集路径
        List<Long> paths = new ArrayList<>();
        //调用递归收集父分类
        paths = findParentPath(catelogId, paths); //[255,25,2]
        //反转一下
        Collections.reverse(paths);

        return paths.toArray(new Long[paths.size()]);//把集合变数组
    }

    /**
     * 级联更新所有数据
     *
     * @param category
     */
    @Transactional//开启事务
    @Override
    public void updateCascade(CategoryEntity category) {
        //先进行常规的更新自己
        //updateById和update方法的区别，前者用于有实体主id根据id查，后者没主id，只能传入特定的更新条件
        this.updateById(category);
        //再更新冗余数据
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    //[255,25,2]
    //递归收集所有父分类
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //先把当前id放进集合
        paths.add(catelogId);
        //搜索这个id的实体
        CategoryEntity byId = this.getById(catelogId);
        //找到这个实体的父id
        if (byId.getParentCid() != 0) {//不等于0就代表有父分类
            //就继续查
            findParentPath(byId.getParentCid(), paths);//查到一个放一个
        }

        return paths;
    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {//传递过来一级分类和所有分类

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {//找到二级分类
            return categoryEntity.getParentCid() == root.getCatId();//条件
        }).map(categoryEntity -> {//拿着二级分类
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));//递归找三级分类
            return categoryEntity;
        }).sorted((menu1, menu2) -> {//排序
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());//收集好

        return children;
    }


}