package cn.weiguangfu.swagger2.plus.enums;

/**
 * API参数执行动作枚举类
 * @author 魏广甫
 * @version 2.7.0-1-beta1
 */
public enum ApiExecutionEnum {

    /**
     * 包含动作,当前方式为包含,请求参数配置此种方式,默认参数全部不展示
     * 参数中只有配置允许的字段才能展示.
     */
    INCLUDE,

    /**
     * 排除动作,当前方式为排除,请求参数中配置此种方式,默认参数需要全部展示,
     * 配置排除的字段不会展示到swagger页面
     */
    EXCLUDE
}