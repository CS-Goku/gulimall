package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 封装页面所有可能传递过来的关键字
 * catalog3Id=225&keyword=华为&sort=saleCount_asc&hasStock=0/1&brandId=25&brandId=30
 */
@Data
public class SearchParam {

    // 页面传递过来的全文匹配关键字
    private String keyword;

    /** 三级分类id*/
    private Long catalog3Id;
    //排序条件：sort=price/salecount/hotscore_desc/asc
    private String sort;
    // 仅显示有货
    private Integer hasStock;

    /*** 价格区间 */
    private String skuPrice;

    /*** 品牌id 可以多选 */
    private List<Long> brandId;

    /*** 按照属性进行筛选 */
    private List<String> attrs;

    /*** 页码*/
    private Integer pageNum = 1;

    /*** 原生所有查询属性*/
    private String _queryString;
}
