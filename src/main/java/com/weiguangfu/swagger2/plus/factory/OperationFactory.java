package com.weiguangfu.swagger2.plus.factory;

import com.weiguangfu.swagger2.plus.util.ObjectUtil;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.service.Operation;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;

import java.util.List;
import java.util.Set;

public final class OperationFactory {

    private final static String PARAMETERS_FIELD_NAME = "parameters";
    private final static String RESPONSE_MODEL_FIELD_NAME = "responseModel";
    private final static String RESPONSE_MESSAGES_FIELD_NAME = "responseMessages";

    private Operation operation;

    private OperationFactory(Operation operation){
        this.operation = operation;
    }

    public static OperationFactory getOperationFactory(Operation operation){
        return new OperationFactory(operation);
    }

    public Operation getOperation(){
        return this.operation;
    }

    public void setParameters(List<Parameter> parameters){
        ObjectUtil.setFieldValue(this.operation, PARAMETERS_FIELD_NAME, parameters);
    }

    public void setResponseModel(ModelReference responseModel){
        ObjectUtil.setFieldValue(this.operation, RESPONSE_MODEL_FIELD_NAME, responseModel);
    }

    public void setResponseMessages(Set<ResponseMessage> newResponseMessages){
        ObjectUtil.setFieldValue(this.operation, RESPONSE_MESSAGES_FIELD_NAME, newResponseMessages);
    }
}