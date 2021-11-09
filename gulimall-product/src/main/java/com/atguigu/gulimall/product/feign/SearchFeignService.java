package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.es.SkuEsMappingModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {

    //上架商品
    @RequestMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsMappingModel> esModels);
}
