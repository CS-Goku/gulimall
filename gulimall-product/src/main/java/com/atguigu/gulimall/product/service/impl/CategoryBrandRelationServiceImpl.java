package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {

        //拿到品牌id和分类id
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        //通过id获取各自详情实体
        //那就得注入服务接口,拿来用
        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryService.getById(catelogId);

        //拿到他们的名字，再放到关系实体中
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        //最后保存这个实体，到数据库，这个时候携带了名字
        this.save(categoryBrandRelation);
    }

    //冗余数据更新-品牌名 update UpdateWrapper方法
    @Override
    public void updateDetail(Long brandId, String name) {

        //创建实体对象
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        //把新数据设置进去
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        //然后操作数据库保存,这个方法要求有实体和更新的操作
        //sql：UPDATE pms_category_brand_relation set brand_name = ? WHERE brand_id = ?;
        this.update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));

    }

    //冗余数据更新-分类名 自定义sql方法
    @Override
    public void updateCategory(Long catId, String name) {

        this.baseMapper.updateCategory(catId,name);
    }

    /**
     * 获取分类关联的品牌
     *
     * @param catId
     * @return List<BrandEntity> 完整的品牌信息实体，给Controller层方便以后复用
     */
    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        //使用本类的dao来查询，也可以用this.baseMapper
        //获取到存有对应分类id，品牌id的关系表实体集合
        List<CategoryBrandRelationEntity> catelog_id = categoryBrandRelationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        //把实体集合的元素映射出来，用这些数据查询完整的品牌实体，再收集成集合
        List<BrandEntity> collect = catelog_id.stream().map(item -> {
            //拿到品牌id
            Long brandId = item.getBrandId();
            //拿到品牌实体
            BrandEntity byId = brandService.getById(brandId);
            return byId;
        }).collect(Collectors.toList());

        return collect;
    }

}