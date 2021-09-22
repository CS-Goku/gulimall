package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author lufei
 * @email 790002348@qq.com
 * @date 2021-08-08 15:58:45
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
