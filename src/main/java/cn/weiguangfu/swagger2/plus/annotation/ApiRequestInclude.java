package cn.weiguangfu.swagger2.plus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API包含请求分组字段定义注解
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta1
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRequestInclude {

    /**
     * 分组Class对象数组
     */
    Class<?>[] groups() ;
}