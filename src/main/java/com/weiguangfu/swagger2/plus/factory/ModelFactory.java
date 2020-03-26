package com.weiguangfu.swagger2.plus.factory;

import com.google.common.base.Optional;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;

import java.util.Map;
import java.util.Objects;

public class ModelFactory {

    public static Optional<Model> newInstance(Model oldModel, String id, String name){
        if (Objects.isNull(oldModel)) {
            Optional.absent();
        }
        // 创建新的对象模型
        Model newModel = new Model(id,
                name,
                oldModel.getType(),
                oldModel.getQualifiedType(),
                oldModel.getProperties(),
                oldModel.getDescription(),
                oldModel.getBaseModel(),
                oldModel.getDiscriminator(),
                oldModel.getSubTypes(),
                oldModel.getExample());
        return Optional.of(newModel);
    }

    public static Optional<Model> newInstance(Model oldModel, String id, String name,
                                              Map<String, ModelProperty> properties){
        if (Objects.isNull(oldModel)) {
            Optional.absent();
        }
        // 创建新的对象模型
        // 创建新的对象模型
        Model newCheckModel = new Model(id,
                name,
                oldModel.getType(),
                oldModel.getQualifiedType(),
                properties,
                oldModel.getDescription(),
                oldModel.getBaseModel(),
                oldModel.getDiscriminator(),
                oldModel.getSubTypes(),
                oldModel.getExample());
        return Optional.of(newCheckModel);
    }
}