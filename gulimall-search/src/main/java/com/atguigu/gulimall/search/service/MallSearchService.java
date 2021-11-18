package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;

public interface MallSearchService {


    /**
     *
     * @param param 检索所有参数
     * @return 检索结果
     */
    Object search(SearchParam param);
}
