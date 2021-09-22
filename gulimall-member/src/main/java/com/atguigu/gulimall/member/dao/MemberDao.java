package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author lufei
 * @email 790002348@qq.com
 * @date 2021-08-08 16:04:06
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
