package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {

//    private "catalog1Id"
//            "catalog1Id":"11",
//            "catalog3List":Array[14],
//            "id":"61",
//            "name":"流行男鞋"

    private String catalog1Id;
    private List<Catelog3Vo> catalog3List;
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
