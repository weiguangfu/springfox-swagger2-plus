package cn.weiguangfu.swagger2.plus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API请求参数字段是否必填注解
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRequestFieldRequired {

    /**
     * 分组Class对象数组
     */
    Class<?>[] groups() ;

    /**
     * 必填参数, 默认为true, 默认情况下此值不用填写
     */
    boolean required() default true;
}