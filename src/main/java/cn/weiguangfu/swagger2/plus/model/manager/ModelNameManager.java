package cn.weiguangfu.swagger2.plus.model.manager;

import cn.weiguangfu.swagger2.plus.enums.ApiModelTypeEnum;

/**
 * 模板名称管理
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta3
 */
public interface ModelNameManager {
    /**
     * 获取增强后模板名称
     * @param path 请求路径
     * @param apiModelTypeEnum 参数模板类型
     * @param originalModelName 原模板名称
     * @return 增强后模板名称
     */
    String getModelPlusName(String path, ApiModelTypeEnum apiModelTypeEnum, String originalModelName);
}