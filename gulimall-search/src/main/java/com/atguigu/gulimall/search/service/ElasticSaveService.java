package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsMappingModel;

import java.io.IOException;
import java.util.List;

public interface ElasticSaveService {

    boolean productStatusUp(List<SkuEsMappingModel> esModels) throws IOException;
}
