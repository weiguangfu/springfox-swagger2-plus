package cn.weiguangfu.swagger2.plus.plus;

import cn.weiguangfu.swagger2.plus.annotation.ApiGroup;
import cn.weiguangfu.swagger2.plus.annotation.ApiPlus;
import cn.weiguangfu.swagger2.plus.enums.ApiModelTypeEnum;
import cn.weiguangfu.swagger2.plus.enums.ResponseStatusEnum;
import cn.weiguangfu.swagger2.plus.factory.ModelFactory;
import cn.weiguangfu.swagger2.plus.factory.ModelPropertyFactory;
import cn.weiguangfu.swagger2.plus.factory.ModelRefFactory;
import cn.weiguangfu.swagger2.plus.factory.OperationFactory;
import cn.weiguangfu.swagger2.plus.filter.FilterField;
import cn.weiguangfu.swagger2.plus.filter.GroupFilterField;
import cn.weiguangfu.swagger2.plus.model.manager.CompletePathModelNameManager;
import cn.weiguangfu.swagger2.plus.model.manager.ModelNameManager;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
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
 * @version 2.7.0
 * @since 2.7.0-1-beta1
 */
@Component
@Import({GroupFilterField.class, CompletePathModelNameManager.class})
public class DefaultSwagger2Push implements Swagger2Push {

    @Autowired
    private FilterField filterField;
    @Autowired
    private ModelNameManager modelNameManager;

    @Value("${swagger.push.enable:false}")
    private boolean enable;

    /**
     * 增强参数对象
     * @author 魏广甫
     * @version 2.7.0
     * @since 2.7.0-1-beta1
     */
    private class EnhanceParameter {
        /** 请求路径 */
        private String path;
        /** 请求方式 */
        private String uniqueid;
        /** 模型定义类型 */
        private ApiModelTypeEnum modelType;
        /** 请求映射上下文 */
        private RequestMappingContext requestMappingContext;
        /** SwaggerApi定义列表 */
        private Map<String, Model> models;

        EnhanceParameter(RequestMappingContext requestMappingContext, Map<String, Model> models) {
            this.requestMappingContext = requestMappingContext;
            this.models = models;
        }

        String getPath() {
            return path;
        }

        void setPath(String path) {
            this.path = path;
        }

        public String getUniqueid() {
            return uniqueid;
        }

        public void setUniqueid(String uniqueid) {
            this.uniqueid = uniqueid;
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

        RequestMappingContext getRequestMappingContext() {
            return requestMappingContext;
        }

        Map<String, Model> getModels() {
            return models;
        }
    }

    @Override
    public List<ApiDescription> getNewApiDescriptionList(ResourceGroup resourceGroup,
             RequestMappingContext requestMappingContext, Map<String, Model> models,
                                                         List<ApiDescription> apiDescriptionList) {

        if (!enable || CollectionUtils.isEmpty(apiDescriptionList) || !isPlus(resourceGroup)) {
            return apiDescriptionList;
        }

        if (!requestMappingContext.findAnnotation(ApiGroup.class).isPresent()) {
            return apiDescriptionList;
        }

        EnhanceParameter enhanceParameter = new EnhanceParameter(requestMappingContext, models);

        for (ApiDescription apiDescription : apiDescriptionList) {
            List<Operation> operationList = apiDescription.getOperations();
            if (CollectionUtils.isEmpty(operationList)) {
                continue;
            }
            enhanceParameter.setPath(apiDescription.getPath());
            for (Operation operation : operationList) {
                enhanceParameter.setUniqueid(operation.getUniqueId());
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
        String path = enhanceParameter.getPath();
        String uniqueid = enhanceParameter.getUniqueid();
        ApiModelTypeEnum modelType = enhanceParameter.getModelType();
        String newDefinitionsKey = modelNameManager.getModelPlusName(path, uniqueid, modelType, type);
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
                if (filterField.isFilterField(enhanceParameter.getRequestMappingContext(),
                        enhanceParameter.getModelType(), erasedTypeClass, oldModelPropertyEntry.getKey())) {
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
}