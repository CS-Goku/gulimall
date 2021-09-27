package com.atguigu.gulimall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        //需求：在分类里点查询，可以搜到对应的分类属性，点击查询全部就是查所有，搜key也可以模糊查询所有


        //前端有个key
        String key = (String) params.get("key");
        //key是点完了三级分类，然后在搜索框里再输入搜的，所以要模糊查询，先查三级分类，再去查其他两个字段

        //先查三级分类信息
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        //不管id是不是0都要模糊查询
        if (!StringUtils.isEmpty(key)) {//如果包含key
            //就接着查
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });//现在wrapper就是一个完整的查询信息
        }

        if (catelogId == 0) {//三级分类id等于0就查询所有
            //Query返回分页信息，QueryWrapper封装查询条件
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);//这些信息放到工具类，返回属性列表，和分页信息
        } else {//三级分类不等于0的情况,有额外的根据三级分类id查询条件
            //select * from pms_attr_group where catelog_id =? and (attr_group_id=key or attr_group_name=key)
            wrapper.eq("catelog_id", catelogId);
            //最后封装到page返回
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }

    }


}