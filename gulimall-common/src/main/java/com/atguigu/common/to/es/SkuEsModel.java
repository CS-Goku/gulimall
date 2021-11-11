package com.atguigu.common.to.es;

/*
"skuId":{ "type": "long" },
            "spuId":{ "type": "keyword" },  # 不可分词
            "skuTitle": {
                "type": "text",
                "analyzer": "ik_smart"  # 中文分词器
            },
            "skuPrice": { "type": "keyword" },  # 保证精度问题
            "skuImg"  : { "type": "keyword" },  # 视频中有false
            "saleCount":{ "type":"long" },
            "hasStock": { "type": "boolean" },
            "hotScore": { "type": "long"  },
            "brandId":  { "type": "long" },
            "catalogId": { "type": "long"  },
            "brandName": {"type": "keyword"}, # 视频中有false
            "brandImg":{
                "type": "keyword",
                "index": false,  # 不可被检索，不生成index，只用做页面使用
                "doc_values": false # 不可被聚合，默认为true
            },
            "catalogName": {"type": "keyword" }, # 视频里有false
            "attrs": {
                "type": "nested",
                "properties": {
                    "attrId": {"type": "long"  },
                    "attrName": {
                        "type": "keyword",
                        "index": false,
                        "doc_values": false
                    },
                    "attrValue": {"type": "keyword" }
 */

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    //库存状态
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

}
