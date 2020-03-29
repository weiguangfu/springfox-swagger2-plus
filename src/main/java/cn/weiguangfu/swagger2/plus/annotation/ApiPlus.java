package cn.weiguangfu.swagger2.plus.annotation;

import java.lang.annotation.*;

/**
 * API增强注解, 通过此注解标记请求类(Controller)是否被增强
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ApiPlus {

    /**
     * 增强标记, 此字段标记是否可以被增强:
     *  true=增强, false=不增强(默认).
     * @return 增强标记
     */
    boolean value() default false;

}