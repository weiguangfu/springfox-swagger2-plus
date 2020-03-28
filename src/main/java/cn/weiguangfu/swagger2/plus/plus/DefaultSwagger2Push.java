package com.weiguangfu.swagger2.plus;

import com.weiguangfu.swagger2.annotation.*;
import com.weiguangfu.swagger2.enums.ApiExecutionEnum;
import com.weiguangfu.swagger2.enums.ApiModelTypeEnum;
import com.weiguangfu.swagger2.enums.ResponseStatusEnum;
import com.weiguangfu.swagger2.factory.ModelFactory;
import com.weiguangfu.swagger2.factory.ModelPropertyFactory;
import com.weiguangfu.swagger2.factory.ModelRefFactory;
import com.weiguangfu.swagger2.factory.OperationFactory;
import com.weiguangfu.swagger2.util.ObjectUtil;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认Swagger2增强对象
 * @author 魏广甫
 * @version 2.7.0-1-beta1
 */
@Component
public class DefaultSwagger2Push implements Swagger2Push {

    @Value("${swagger.push.enable:false}")
    private boolean enable;

    /**
     * 增强参数对象
     * @author 魏广甫
     * @version 2.7.0-1-beta1
     */
    private class EnhanceParameter {
        /** 请求路径 */
        private String path;
        /** 模型定义类型 */
        private ApiModelTypeEnum modelType;
        /** 请求映射上下文 */
        private RequestMappingContext each;
        /** SwaggerApi定义列表 */
        private Map<String, Model> models;

        EnhanceParameter(RequestMappingContext each, Map<String, Model> models) {
            this.each = each;
            this.models = models;
        }

        String getPath() {
            return path;
        }

        void setPath(String path) {
            this.path = path;
        }

        ApiModelTypeEnum getModelType() {
            return modelType;
        }

        void setModelType(ApiModelTypeEnum modelType) {
            this.modelType = modelType;
        }

        void removeModelType() {
            this.modelType = null;
        }

        RequestMappingContext getEach() {
            return each;
        }

        Map<String, Model> getModels() {
            return models;
        }
    }

    @Override
    public List<ApiDescription> getNewApiDescriptionList(ResourceGroup resourceGroup,
             RequestMappingContext each, Map<String, Model> models, List<ApiDescription> apiDescriptionList) {

        if (!enable || CollectionUtils.isEmpty(apiDescriptionList) || !isPlus(resourceGroup)) {
            return apiDescriptionList;
        }

        if (!each.findAnnotation(ApiGroup.class).isPresent()) {
            return apiDescriptionList;
        }

        EnhanceParameter enhanceParameter = new EnhanceParameter(each, models);

        for (ApiDescription apiDescription : apiDescriptionList) {
            List<Operation> operationList = apiDescription.getOperations();
            if (CollectionUtils.isEmpty(operationList)) {
                continue;
            }
            for (Operation operation : operationList) {
                enhanceParameter.setPath(apiDescription.getPath());
                enhanceOperation(enhanceParameter, operation);
            }
        }
        return apiDescriptionList;
    }

    /**
     * API是否增强判断
     * @param resourceGroup 请求Controller类上下文
     * @return true: 需要增强, false: 不需要增强
     */
    private boolean isPlus(ResourceGroup resourceGroup) {
        if (Objects.isNull(resourceGroup)) {
            return false;
        }
        if (resourceGroup.getControllerClass().isPresent()) {
            Class<?> controllerClass = resourceGroup.getControllerClass().get();
            ApiPlus apiPlus = controllerClass.getAnnotation(ApiPlus.class);
            if (Objects.nonNull(apiPlus)) {
                return apiPlus.value();
            }
        }
        return false;
    }

    private Operation enhanceOperation(EnhanceParameter enhanceParameter, Operation operation){
        OperationFactory operationFactory = OperationFactory.getOperationFactory(operation);
        // 请求参数增强
        List<Parameter> parameterList = operation.getParameters();
        if (!CollectionUtils.isEmpty(parameterList)) {
            List<Parameter> newParameterList = new ArrayList<>(parameterList.size());
            for (Parameter parameter : parameterList) {
                newParameterList.add(enhanceParameter(enhanceParameter, parameter));
            }
            operationFactory.setParameters(newParameterList);
        }

        // 响应参数增强
        ModelReference responseModel
                = enhanceModelReference(enhanceParameter, ApiModelTypeEnum.RESPONSE, operation.getResponseModel());
        operationFactory.setResponseModel(responseModel);

        // 响应成功时的参数增强
        operationFactory.setResponseMessages(enhanceResponseMessage(operation.getResponseMessages(), responseModel));
        return operationFactory.getOperation();
    }

    private Parameter enhanceParameter(EnhanceParameter enhanceParameter, Parameter parameter) {
        return new Parameter(parameter.getName(),
                parameter.getDescription(),
                parameter.getDefaultValue(),
                parameter.isRequired(),
                parameter.isAllowMultiple(),
                enhanceModelReference(enhanceParameter, ApiModelTypeEnum.REQUEST, parameter.getModelRef()),
                parameter.getType(),
                parameter.getAllowableValues(),
                parameter.getParamType(),
                parameter.getParamAccess(),
                parameter.isHidden(),
                parameter.getVendorExtentions());
    }

    private Set<ResponseMessage> enhanceResponseMessage(Set<ResponseMessage> responseMessageSet,
                                                        ModelReference responseModel){
        if (CollectionUtils.isEmpty(responseMessageSet)) {
            return new HashSet<>();
        }
        Set<ResponseMessage> newResponseMessageSet = new HashSet<>(responseMessageSet.size());
        int okStatus = ResponseStatusEnum.OK.getStatus();
        for (ResponseMessage responseMessage : responseMessageSet) {
            if (Objects.equals(okStatus, responseMessage.getCode())) {
                responseMessage = new ResponseMessage(responseMessage.getCode(),
                        responseMessage.getMessage(),
                        responseModel,
                        responseMessage.getHeaders(),
                        responseMessage.getVendorExtensions());
            }
            newResponseMessageSet.add(responseMessage);
        }
        return newResponseMessageSet;
    }

    private ModelReference enhanceModelReference(EnhanceParameter enhanceParameter, ApiModelTypeEnum modelTypeEnum,
                                           ModelReference modelReference) {
        try {
            enhanceParameter.setModelType(modelTypeEnum);
            Optional<ModelReference> newModelReferenceOptional
                    = getNewModelReference(enhanceParameter, modelReference);
            if (newModelReferenceOptional.isPresent()) {
                return newModelReferenceOptional.get();
            }
        } finally {
            enhanceParameter.removeModelType();
        }
        return modelReference;
    }

    private Optional<Model> getAndCreaeMtodel(EnhanceParameter enhanceParameter, String type){
        Map<String, Model> models = enhanceParameter.getModels();
        String newDefinitionsKey = getTypeName(enhanceParameter, type);
        Model newDefinitionsModel = models.get(newDefinitionsKey);
        // 原定义列表中没有此定义
        if (Objects.isNull(newDefinitionsModel)) {
            Model oldModel = models.get(type);
            // 原定义中也没有定义, 忽略此定义类型
            if (Objects.isNull(oldModel)) {
                return Optional.absent();
            } else {
                return createModel(enhanceParameter, oldModel, newDefinitionsKey);
            }
        } else {
            return Optional.of(newDefinitionsModel);
        }
    }

    private Optional<Model> createModel(EnhanceParameter enhanceParameter, Model oldModel, String newDefinitionsKey) {
        Map<String, Model> models = enhanceParameter.getModels();
        // 创建一个新的定义
        // 创建新的对象模型(未检测字段列表)
        Optional<Model> newModelOptional
                = ModelFactory.newInstance(oldModel, newDefinitionsKey, newDefinitionsKey);
        if (newModelOptional.isPresent()) {
            models.put(newDefinitionsKey, newModelOptional.get());
        }
        // 新字段列表
        Map<String, ModelProperty> newPropertieMap = new ConcurrentHashMap<>();
        // 遍历对象中属性
        Map<String, ModelProperty> oldPropertieMap = oldModel.getProperties();
        if (!CollectionUtils.isEmpty(oldPropertieMap)) {
            Class<?> erasedTypeClass = oldModel.getType().getErasedType();
            for (Map.Entry<String, ModelProperty> oldModelPropertyEntry : oldPropertieMap.entrySet()) {
                if (isSkipProperty(enhanceParameter, erasedTypeClass, oldModelPropertyEntry.getKey())) {
                    continue;
                }
                ModelProperty newModelProperty
                        = getNewModelProperty(enhanceParameter, oldModelPropertyEntry.getValue());
                newPropertieMap.put(oldModelPropertyEntry.getKey(), newModelProperty);
            }
            // 创建新的对象模型(包含所有新字段)
            newModelOptional
                    = ModelFactory.newInstance(oldModel, newDefinitionsKey, newDefinitionsKey, newPropertieMap);
            if (newModelOptional.isPresent()) {
                models.put(newDefinitionsKey, newModelOptional.get());
            }
        }
        return newModelOptional;
    }

    /**
     * 判断是否跳过字段
     * @return true: 跳过, false: 不跳过
     */
    private boolean isSkipProperty(EnhanceParameter enhanceParameter, Class<?> erasedTypeClass, String fieldName) {
        RequestMappingContext each = enhanceParameter.getEach();
        if (!each.findAnnotation(ApiGroup.class).isPresent()) {
            return false;
        }
        ApiGroup apiGroup = each.findAnnotation(ApiGroup.class).get();
        Class<?>[] apiGroups = apiGroup.groups();
        // 请求包含处理
        if (Objects.equals(enhanceParameter.getModelType(), ApiModelTypeEnum.REQUEST)
                && Objects.equals(apiGroup.requestExecution(), ApiExecutionEnum.INCLUDE)) {
            ApiRequestInclude apiRequestInclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiRequestInclude.class);
            return Objects.isNull(apiRequestInclude)
                    || !ObjectUtil.isSingleEquals(apiRequestInclude.groups(), apiGroups);
            // 请求排除处理
        } else if (Objects.equals(enhanceParameter.getModelType(), ApiModelTypeEnum.REQUEST)
                && Objects.equals(apiGroup.requestExecution(), ApiExecutionEnum.EXCLUDE)) {
            ApiRequestExclude apiRequestExclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiRequestExclude.class);
            return Objects.nonNull(apiRequestExclude)
                    && ObjectUtil.isSingleEquals(apiRequestExclude.groups(), apiGroups);
            // 响应包含处理
        } else if (Objects.equals(enhanceParameter.getModelType(), ApiModelTypeEnum.RESPONSE)
                && Objects.equals(apiGroup.responseExecution(), ApiExecutionEnum.INCLUDE)) {
            ApiResponseInclude apiResponseInclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiResponseInclude.class);
            return Objects.isNull(apiResponseInclude)
                    || !ObjectUtil.isSingleEquals(apiResponseInclude.groups(), apiGroups);
        } else if (Objects.equals(enhanceParameter.getModelType(), ApiModelTypeEnum.RESPONSE)
                && Objects.equals(apiGroup.responseExecution(), ApiExecutionEnum.EXCLUDE)) {
            ApiResponseExclude apiResponseExclude
                    = ObjectUtil.getFieldAnnotation(erasedTypeClass, fieldName, ApiResponseExclude.class);
            return Objects.nonNull(apiResponseExclude)
                    && ObjectUtil.isSingleEquals(apiResponseExclude.groups(), apiGroups);
        }
        return false;
    }

    private ModelProperty getNewModelProperty(EnhanceParameter enhanceParameter, ModelProperty oldModelProperty) {
        ModelProperty newModelProperty = oldModelProperty;
        Optional<ModelProperty> newModelPropertyOptional = ModelPropertyFactory.newInstance(oldModelProperty);
        if (newModelPropertyOptional.isPresent()) {
            newModelProperty = newModelPropertyOptional.get();
            Optional<ModelReference> newModelReferenceOptional
                    = getNewModelReference(enhanceParameter, oldModelProperty.getModelRef());
            if (newModelReferenceOptional.isPresent()) {
                newModelProperty.updateModelRef(input -> newModelReferenceOptional.get());
            }
        }
        return newModelProperty;
    }

    private Optional<ModelReference> getNewModelReference(
            EnhanceParameter enhanceParameter, ModelReference modelReference){
        if (Objects.isNull(modelReference)) {
            return Optional.absent();
        }
        // 检查是否为特殊字段
        // 特殊字段
        if (modelReference.itemModel().isPresent()) {
            ModelReference specialItemModelReference = modelReference.itemModel().get();
            Optional<ModelReference> finalSpecialItemModelOptional
                    = getSpecialItemModel(enhanceParameter, specialItemModelReference);
            if (finalSpecialItemModelOptional.isPresent()) {
                return ModelRefFactory.newInstance(modelReference, finalSpecialItemModelOptional.get());
            }
        // 普通字段
        } else {
            String type = modelReference.getType();
            Optional<Model> modelOptional = getAndCreaeMtodel(enhanceParameter, type);
            if (modelOptional.isPresent()) {
                return ModelRefFactory.newInstance(modelReference, modelOptional.get().getId());
            }
        }
        return Optional.of(modelReference);
    }

    private Optional<ModelReference> getSpecialItemModel(
            EnhanceParameter enhanceParameter, ModelReference specialItemModelReference) {
        ModelReference newModelReference = specialItemModelReference;
        Optional<ModelReference> newModelReferenceOptional = ModelRefFactory.newInstance(specialItemModelReference);
        if (newModelReferenceOptional.isPresent()) {
            newModelReference = newModelReferenceOptional.get();
        }
        Optional<ModelReference> modelReferenceOptional = specialItemModelReference.itemModel();
        if (modelReferenceOptional.isPresent()) {
            Optional<ModelReference> nextSpecialItemModelOptional
                    = getSpecialItemModel(enhanceParameter, modelReferenceOptional.get());
            if (nextSpecialItemModelOptional.isPresent()) {
                Optional<ModelReference> newNextModelReferenceOptional
                        = ModelRefFactory.newInstance(newModelReference, nextSpecialItemModelOptional.get());
                if (newNextModelReferenceOptional.isPresent()) {
                    newModelReference = newNextModelReferenceOptional.get();
                }
            }
        } else {
            Optional<Model> modelOptional = getAndCreaeMtodel(enhanceParameter, specialItemModelReference.getType());
            if (modelOptional.isPresent()) {
                Optional<ModelReference> newNextModelReferenceOptional
                        = ModelRefFactory.newInstance(newModelReference, modelOptional.get().getId());
                if (newNextModelReferenceOptional.isPresent()) {
                    newModelReference = newNextModelReferenceOptional.get();
                }
            }
        }
        return Optional.of(newModelReference);
    }

    private String getTypeName(EnhanceParameter enhanceParameter, String type) {
        String path = Objects.nonNull(enhanceParameter.getPath()) ? enhanceParameter.getPath() : "";
        ApiModelTypeEnum apiModelTypeEnum = enhanceParameter.getModelType();
        String apiModelType = Objects.nonNull(apiModelTypeEnum) ? apiModelTypeEnum.getModelType() : "";
        return path.replace("/", "_") + "_" + apiModelType + "_" + type;
    }
}