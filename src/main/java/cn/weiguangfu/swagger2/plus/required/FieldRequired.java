package cn.weiguangfu.swagger2.plus.required;

import cn.weiguangfu.swagger2.plus.annotation.ApiRequestFieldRequired;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

/**
 * 字段是否必填控制接口
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1
 */
public interface FieldRequired {

    /**
     * 是否为必填字段, 根据注解{@link ApiRequestFieldRequired}确定字段是否需要必填.
     * @param requestMappingContext swaggerApi请求映射上下文
     * @param erasedTypeClass 字段所在Class对象
     * @param fieldName 字段名称
     * @return true: 字段必填, false: 字段非必填
     */
    boolean isRequestRequired(RequestMappingContext requestMappingContext, Class<?> erasedTypeClass, String fieldName);
}