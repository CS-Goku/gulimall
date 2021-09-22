package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author lufei
 * @email 790002348@qq.com
 * @date 2021-08-08 15:58:03
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
