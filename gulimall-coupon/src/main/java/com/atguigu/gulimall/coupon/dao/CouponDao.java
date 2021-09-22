package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author lufei
 * @email 790002348@qq.com
 * @date 2021-08-08 16:02:38
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
