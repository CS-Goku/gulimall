package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.BaseAttrs;
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

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {
    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttrValue(Long id, List<BaseAttrs> baseAttrs) {
        if (baseAttrs == null || baseAttrs.size() == 0){

        }else {
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(id);
                productAttrValueEntity.setAttrId(item.getAttrId());
                //需要另外查询属性名字，根据属性id
                AttrEntity byId = attrService.getById(item.getAttrId());
                productAttrValueEntity.setAttrName(byId.getAttrName());
                productAttrValueEntity.setAttrValue(item.getAttrValues());
                productAttrValueEntity.setQuickShow(item.getShowDesc());

                return productAttrValueEntity;
            }).collect(Collectors.toList());
            this.saveBatch(collect);
        }
    }

}