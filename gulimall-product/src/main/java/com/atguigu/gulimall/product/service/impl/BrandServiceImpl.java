package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //找到这个key参数
        String key = (String) params.get("key");
        //先查询所有
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        //再判断key有没有
        if (!StringUtils.isEmpty(key)){
            //存在key,接着条件查询sql：select * from pms_brand WHERE brand_id = key or name = key;
            wrapper.and((obj)->{
                obj.eq("brand_id",key).or().like("name",key);
            });
        }
        //最后封装给page
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void updateDetail(BrandEntity brand) {
        //品牌名字变了，那么关系表中的冗余字段也要跟着变，因为冗余字段是自己单独另外存进去的数据，不是关联查询出来的，所以要额外设定一个细节就是保证冗余的数据一致
        //先保证正常的信息更新
        this.updateById(brand);
        //如果其中有name改动，那么分类与品牌的关系表的品牌name也要更新，name是冗余字段
        //sql：error: UPDATE  SET 字段名 = 新值 WHERE 字段名 = 某值
        //UPDATE pms_category_brand_relation set brand_name = "华为1" WHERE brand_id = 2;
        if(!StringUtils.isEmpty(brand.getName())){
            categoryBrandRelationService.updateDetail(brand.getBrandId(),brand.getName());
        }


    }

}