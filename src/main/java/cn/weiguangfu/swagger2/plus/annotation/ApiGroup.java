package com.weiguangfu.swagger2.annotation;

import com.weiguangfu.swagger2.enums.ApiExecutionEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API分组
 * @author 魏广甫
 * @version 2.7.0-1-beta1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiGroup {

    /**
     * 字段过滤分组对象
     */
    Class<?>[] groups() ;

    /**
     * 请求参数执行动作, 增强时进行处理的动作(默认为包含动作). 详细查询动作枚举在
     * {@link ApiExecutionEnum}
     */
    ApiExecutionEnum requestExecution() default ApiExecutionEnum.INCLUDE;

    /**
     * 响应参数执行动作, 增强时进行处理的动作(默认为包含动作). 详细查询动作枚举在
     * {@link ApiExecutionEnum}
     */
    ApiExecutionEnum responseExecution() default ApiExecutionEnum.INCLUDE;
}