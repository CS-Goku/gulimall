package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

public interface MallSearchService {


    /**
     *
     * @param param 检索所有参数
     * @return 返回页面需要的展示的数据
     */
    SearchResult search(SearchParam param);
}
