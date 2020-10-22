package cn.weiguangfu.swagger2.plus.required;

import cn.weiguangfu.swagger2.plus.annotation.ApiGroup;
import cn.weiguangfu.swagger2.plus.annotation.ApiRequestFieldRequired;
import cn.weiguangfu.swagger2.plus.util.ObjectUtil;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.util.Objects;

/**
 * 分组字段是否必填控制
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1
 */
@Component
public class GroupFieldRequired implements FieldRequired {

    @Override
    public boolean isRequestRequired(RequestMappingContext requestMappingContext,
                                     Class<?> erasedTypeClass, String fieldName) {
        if (!requestMappingContext.findAnnotation(ApiGroup.class).isPresent()) {
            return false;
        }
        ApiGroup apiGroup = requestMappingContext.findAnnotation(ApiGroup.class).get();
        Class<?>[] apiGroupArray = apiGroup.groups();

        ApiRequestFieldRequired apiRequestFieldRequired
                = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiRequestFieldRequired.class);
        if (Objects.nonNull(apiRequestFieldRequired) && apiRequestFieldRequired.required()) {
            return ObjectUtil.isSingleEquals(apiRequestFieldRequired.groups(), apiGroupArray);
        }
        return false;
    }
}