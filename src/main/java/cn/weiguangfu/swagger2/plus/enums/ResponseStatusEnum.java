package com.weiguangfu.swagger2.enums;

/**
 * 响应状态码枚举类
 * @author 魏广甫
 * @version 2.7.0-1-beta1
 */
public enum ResponseStatusEnum {

    /** Http请求响应成功响应码 */
    OK(200);

    private int status;

    ResponseStatusEnum(int status){
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}