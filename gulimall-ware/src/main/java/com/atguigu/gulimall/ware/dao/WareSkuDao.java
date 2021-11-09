package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author lufei
 * @email 790002348@qq.com
 * @date 2021-08-08 16:04:54
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    Long selectHasStockBySkuId(@Param("skuId") Long skuId);
}
