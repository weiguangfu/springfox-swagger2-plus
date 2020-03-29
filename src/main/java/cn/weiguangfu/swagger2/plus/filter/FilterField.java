package cn.weiguangfu.swagger2.plus.filter;

import cn.weiguangfu.swagger2.plus.enums.ApiModelTypeEnum;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

/**
 * 字段过滤
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta3
 */
public interface FilterField {

    /**
     * 是否过滤字段
     * @param requestMappingContext swaggerApi请求映射上下文
     * @param apiModelTypeEnum 模板类型
     * @param erasedTypeClass 字段所在Class对象
     * @param fieldName 字段名称
     * @return true:过滤, false:不过滤
     */
    boolean isFilterField(RequestMappingContext requestMappingContext, ApiModelTypeEnum apiModelTypeEnum,
                          Class<?> erasedTypeClass, String fieldName);
}