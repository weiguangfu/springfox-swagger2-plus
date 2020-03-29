package cn.weiguangfu.swagger2.plus.factory;

import com.google.common.base.Optional;
import springfox.documentation.schema.ModelProperty;

import java.util.Objects;

public final class ModelPropertyFactory {

    public static Optional<ModelProperty> newInstance(ModelProperty oldModelProperty){
        if (Objects.isNull(oldModelProperty)) {
            return Optional.absent();
        }
        ModelProperty newModelProperty = new ModelProperty(oldModelProperty.getName(),
                oldModelProperty.getType(),
                oldModelProperty.getQualifiedType(),
                oldModelProperty.getPosition(),
                oldModelProperty.isRequired(),
                oldModelProperty.isHidden(),
                oldModelProperty.isReadOnly(),
                oldModelProperty.getDescription(),
                oldModelProperty.getAllowableValues(),
                oldModelProperty.getExample(),
                oldModelProperty.getPattern(),
                oldModelProperty.getVendorExtensions());
        return Optional.of(newModelProperty);
    }
}