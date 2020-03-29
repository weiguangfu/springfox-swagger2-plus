package cn.weiguangfu.swagger2.plus.enums;

/**
 * API文档定义类型(请求定义, 响应定义...)
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta1
 */
public enum ApiModelTypeEnum {

    /** 请求类型 */
    REQUEST("request"),

    /** 响应类型 */
    RESPONSE("response");

    private String modelType;

    ApiModelTypeEnum(String modelType) {
        this.modelType = modelType;
    }

    public String getModelType() {
        return modelType;
    }
}