package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * couponFeignService.saveSpuBounds(spuBoundTo);
     * 1、@RequestBody 将这个对象转为json
     * 2、将上一步的json放到请求体位置，找到coupon服务，发送请求
     * 3、对方服务收到请求将json转为SpuBoundsEntity，只要json兼容，无需公用同一个to
     *
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);


    @PostMapping("/coupon/skufullreduction/saveinfo")//没有方法可以复用，要自己创建一个，to的json也对不上号
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
