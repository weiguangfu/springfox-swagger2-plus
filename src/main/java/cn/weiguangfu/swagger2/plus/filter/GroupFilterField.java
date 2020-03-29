package cn.weiguangfu.swagger2.plus.filter;

import cn.weiguangfu.swagger2.plus.annotation.*;
import cn.weiguangfu.swagger2.plus.enums.ApiExecutionEnum;
import cn.weiguangfu.swagger2.plus.enums.ApiModelTypeEnum;
import cn.weiguangfu.swagger2.plus.util.ObjectUtil;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.util.Objects;

/**
 * 分组字段过滤
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta3
 */
@Component
public class GroupFilterField implements FilterField {
    @Override
    public boolean isFilterField(RequestMappingContext requestMappingContext, ApiModelTypeEnum apiModelTypeEnum, Class<?> erasedTypeClass, String fieldName) {
        if (!requestMappingContext.findAnnotation(ApiGroup.class).isPresent()) {
            return false;
        }
        ApiGroup apiGroup = requestMappingContext.findAnnotation(ApiGroup.class).get();
        Class<?>[] apiGroups = apiGroup.groups();
        // 请求包含处理
        if (Objects.equals(apiModelTypeEnum, ApiModelTypeEnum.REQUEST)
                && Objects.equals(apiGroup.requestExecution(), ApiExecutionEnum.INCLUDE)) {
            ApiRequestInclude apiRequestInclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiRequestInclude.class);
            return Objects.isNull(apiRequestInclude)
                    || !ObjectUtil.isSingleEquals(apiRequestInclude.groups(), apiGroups);
            // 请求排除处理
        } else if (Objects.equals(apiModelTypeEnum, ApiModelTypeEnum.REQUEST)
                && Objects.equals(apiGroup.requestExecution(), ApiExecutionEnum.EXCLUDE)) {
            ApiRequestExclude apiRequestExclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiRequestExclude.class);
            return Objects.nonNull(apiRequestExclude)
                    && ObjectUtil.isSingleEquals(apiRequestExclude.groups(), apiGroups);
            // 响应包含处理
        } else if (Objects.equals(apiModelTypeEnum, ApiModelTypeEnum.RESPONSE)
                && Objects.equals(apiGroup.responseExecution(), ApiExecutionEnum.INCLUDE)) {
            ApiResponseInclude apiResponseInclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiResponseInclude.class);
            return Objects.isNull(apiResponseInclude)
                    || !ObjectUtil.isSingleEquals(apiResponseInclude.groups(), apiGroups);
        } else if (Objects.equals(apiModelTypeEnum, ApiModelTypeEnum.RESPONSE)
                && Objects.equals(apiGroup.responseExecution(), ApiExecutionEnum.EXCLUDE)) {
            ApiResponseExclude apiResponseExclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiResponseExclude.class);
            return Objects.nonNull(apiResponseExclude)
                    && ObjectUtil.isSingleEquals(apiResponseExclude.groups(), apiGroups);
        }
        return false;
    }
}