package com.atguigu.gulimall.product.feign;


import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @RequestMapping("/hasStock")
    public R<List<SkuHasStockVo>> getHasStock(@RequestBody List<Long> skuIds);
}
