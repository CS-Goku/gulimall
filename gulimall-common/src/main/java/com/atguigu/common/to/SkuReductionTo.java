package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuReductionTo {

    private Long skuId;
    //满几件打几折
    private int fullCount;
    private BigDecimal discount;
    //是否参与其他优惠
    private int countStatus;

    //满多少减多少
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    //是否参与其他优惠
    private int priceStatus;

    //会员价格
    private List<MemberPrice> memberPrice;
}
