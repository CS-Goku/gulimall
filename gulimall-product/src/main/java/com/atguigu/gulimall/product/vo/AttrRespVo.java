package com.atguigu.gulimall.product.vo;


import lombok.Data;

@Data
public class AttrRespVo extends AttrVo{

    /*
    			"catelogName": "手机/数码/手机", //所属分类名字
			    "groupName": "主体", //所属分组名字
     */

    private String catelogName;//所属分类名字

    private String groupName;//所属属性分组名字


}
