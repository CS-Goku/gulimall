package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsMappingModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    AttrService attrService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * //todo 高级部分完善
     *
     * @param vo
     */
    @Transactional
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
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);

        //5、保存spu积分信息：sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //6、保存当前spu对应的sku信息：
        List<Skus> skus = vo.getSkus();
        skus.forEach(item -> {//todo forEach、stream.map、for的区别是什么
            //6、1保存sku基本信息：pms_sku_info
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
            skuInfoService.saveSkuInfo(skuInfoEntity);

            Long skuId = skuInfoEntity.getSkuId();
            //6、2保存sku图片信息：pms_sku_images
            List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setImgUrl(img.getImgUrl());
                skuImagesEntity.setDefaultImg(img.getDefaultImg());
                return skuImagesEntity;
            }).filter(entity -> {
                //返回ture就是需要这个
                return !StringUtils.isEmpty(entity.getImgUrl());
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(imagesEntities);
            // todo 没有图片路径的无需保存

            //6、3保存sku销售属性信息：pms_sku_sale_attr_value
            List<Attr> attr = item.getAttr();
            List<SkuSaleAttrValueEntity> attrValueEntities = attr.stream().map(a -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(attrValueEntities);

            //6、4保存sku的优惠、满减信息：gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            BeanUtils.copyProperties(item, skuReductionTo);
            skuReductionTo.setSkuId(skuId);
            if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                if (r1.getCode() != 0) {
                    log.error("远程保存sku优惠信息失败");
                }
            }
        });


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    /**
     * spu检索，条件参数：分类、品牌、状态、key、分页
     * <p>
     * status: 0
     * key: 18
     * brandId: 14
     * catelogId: 225
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);

    }

    @Override
    public void up(Long spuId) {
        //1.根据spuId查出所有的sku
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);


        //todo 3.公共属性数据同一个spu的sku都一样，所以放到外面查一次,查出所有可被检索的attr集合
//            private List<SkuEsMappingModel.Attr> attrs;
//            @Data
//            public static class Attr{
//                private Long attrId;
//                private String attrName;
//                private String attrValue;
//            }
        //todo 问题：为什么不能用联表查询？为什么要List要转Set
        //3.1根据spuId查对应的商品属性attrids集合
        List<ProductAttrValueEntity> attrs = attrValueService.getAttrBySpuId(spuId);
        List<Long> attrIds = attrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        //3.2根据attrIds的中的id找出属性表中对应的id，以及这些id需要满足可被检索的条件,因为同一个表中两个条件，所以用sql写
        //SELECT attr_id FROM pms_attr WHERE attr_id in (1,2) and search_type = 1;
        List<Long> searchAttrIds = attrService.selectSearchAttrIdsByAttrIds(attrIds);
        //3.3 根据满足条件的集合id过滤出第一个属性集合,在封装成List<SkuEsMappingModel.Attr> attrList，到第2步去设置
        Set<Long> idsSet = new HashSet<>(searchAttrIds);
        List<SkuEsMappingModel.Attr> attrList = attrs.stream().filter(attr -> {
            //List<ProductAttrValueEntity>
            return idsSet.contains(attr.getAttrId());
        }).map(item -> {
            //最终放到es实体的List<Attr> attrs中
            SkuEsMappingModel.Attr attr = new SkuEsMappingModel.Attr();
            BeanUtils.copyProperties(attr, item);
            return attr;
        }).collect(Collectors.toList());



        //拿到所有skuIds给esModel.setHasStock(?);
        Map<Long, Boolean> map = null;
        try {
            List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSpuId).collect(Collectors.toList());
            R<List<SkuHasStockVo>> hasStock = wareFeignService.getHasStock(skuIds);
            List<SkuHasStockVo> data = hasStock.getData();
            map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e){
            log.error("库存服务查询异常：原因{}",e);
        }



        //2.把skus弄到对应的es实体中，先拷贝，在额外添加其他的没有的数据
        Map<Long, Boolean> finalMap = map;
        List<SkuEsMappingModel> esModels = skus.stream().map(sku -> {
            //先拷贝已有数据
            SkuEsMappingModel esModel = new SkuEsMappingModel();
            BeanUtils.copyProperties(sku, esModel);


            //哪些是原实体中没有的数据
            //skuPrice，skuImg
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //查询品牌名字、图片 和 分类名字brandName，brandImg，catalogName
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());
            //热门评分，太复杂，直接默认0
            esModel.setHotScore(0L);

            //todo 1.远程调用库存服务查看库存状态private Boolean hasStock;
            if (finalMap == null){
                esModel.setHasStock(false);
            }else {
                esModel.setHasStock(finalMap.get(sku.getSkuId()));
            }


            //todo 2.放入属性
            esModel.setAttrs(attrList);

            return esModel;
        }).collect(Collectors.toList());
        //3.调用es服务上架
        R r = searchFeignService.productStatusUp(esModels);
        if (r.getCode() == 0){
            //成功，修改spu上架状态
            baseMapper.updateSpuStatus(spuId,ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //失败
            //todo 重复调用，接口幂等性问题；重试机制
        }
    }


}