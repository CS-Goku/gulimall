package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

//第一个泛型参数是所对应的校验注解类型，第二个是校验对象类型
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    // 用来存储指定的值
    private Set<Integer> set = new HashSet<>();
    //初始化方法
    @Override// 通过重写initialize()方法，获得注解上的内容，放在集合里，完成指定实体参数需要指定值
    public void initialize(ListValue constraintAnnotation) {

        int[] vals = constraintAnnotation.vals();//写的注解是@ListValue(value={0,1})
        for (int val : vals) {//遍历放入集合
            set.add(val);
        }

    }


    /**
     *
     * @param value 需要校验的值
     * @param context
     * @return
     *真正的验证逻辑由`isValid`完成，如果传入形参的属性值在这个set里就返回true，否则返回false
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {


        return set.contains(value);
    }
}
