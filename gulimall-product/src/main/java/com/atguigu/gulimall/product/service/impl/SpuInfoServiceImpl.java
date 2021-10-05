package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu基本信息：pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存spu描述图片：pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(infoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3、保存spu图片集：pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);

        //4、保存spu规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveProductAttrValue(infoEntity.getId(), baseAttrs);

        //5、保存spu积分信息：sms_spu_bounds

        //5、保存当前spu对应的sku信息：
        List<Skus> skus = vo.getSkus();
        skus.forEach(item -> {//todo forEach、stream.map、for的区别是什么
            String defaultImg = "";
            for (Images image : item.getImages()) {
                if (image.getDefaultImg() == 1) {
                    defaultImg = image.getImgUrl();
                }
            }
            //    private String skuName;
            //    private BigDecimal price;
            //    private String skuTitle;
            //    private String skuSubtitle;
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(item, skuInfoEntity);
            skuInfoEntity.setBrandId(infoEntity.getBrandId());
            skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setSpuId(infoEntity.getId());
            skuInfoEntity.setSkuDefaultImg(defaultImg);

            //5、1保存sku基本信息：pms_sku_info
            skuInfoService.saveSkuInfo(skuInfoEntity);

            List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                skuImagesEntity.setImgUrl(img.getImgUrl());
                skuImagesEntity.setDefaultImg(img.getDefaultImg());
                return skuImagesEntity;
            }).collect(Collectors.toList());
            //5、2保存sku图片信息：pms_sku_images
            skuImagesService.saveBatch(imagesEntities);

            //5、3保存sku销售属性信息：pms_sku_sale_attr_value
            List<Attr> attr = item.getAttr();
            List<SkuSaleAttrValueEntity> attrValueEntities = attr.stream().map(a -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(a,skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(attrValueEntities);
        });

        //5、4保存sku的优惠、满减信息：gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }


}