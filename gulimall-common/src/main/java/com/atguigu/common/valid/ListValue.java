package com.atguigu.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/*
必须有3个属性：就是注解上可以写的东西
        - message()错误信息
        - groups()分组校验
        - payload()自定义负载信息
 */
@Documented
@Constraint(validatedBy = { ListValueConstraintValidator.class })// 关联校验器和校验注解
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })//哪些地方可以用
@Retention(RUNTIME)
public @interface ListValue {
    String message() default "{com.atguigu.common.valid.ListValue.message}";//消息从哪取得

    Class<?>[] groups() default { };//默认分组校验

    Class<? extends Payload>[] payload() default { };

    //数组，需要自己指定
    int[] vals() default { };
}
