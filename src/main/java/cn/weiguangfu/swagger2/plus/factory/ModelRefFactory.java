package cn.weiguangfu.swagger2.plus.factory;

import com.google.common.base.Optional;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;

import java.util.Objects;

public class ModelRefFactory {

    public static Optional<ModelReference> newInstance(ModelReference oldModelReference) {
        if (Objects.isNull(oldModelReference)) {
            return Optional.absent();
        }
        ModelReference itemType = null;
        if (oldModelReference.itemModel().isPresent()) {
            itemType = oldModelReference.itemModel().get();
        }
        ModelReference modelRef = new ModelRef(oldModelReference.getType(),
                itemType,
                oldModelReference.getAllowableValues(),
                oldModelReference.isMap());

        return Optional.of(modelRef);
    }

    public static Optional<ModelReference> newInstance(ModelReference oldModelReference, String type) {
        if (Objects.isNull(oldModelReference)) {
            return Optional.absent();
        }
        ModelReference itemType = null;
        if (oldModelReference.itemModel().isPresent()) {
            itemType = oldModelReference.itemModel().get();
        }
        ModelReference modelRef = new ModelRef(type,
                itemType,
                oldModelReference.getAllowableValues(),
                oldModelReference.isMap());

        return Optional.of(modelRef);
    }

    public static Optional<ModelReference> newInstance(ModelReference oldModelReference,
                                                       ModelReference itemModelReference) {
        if (Objects.isNull(oldModelReference)) {
            return Optional.absent();
        }
        ModelReference modelRef = new ModelRef(oldModelReference.getType(),
                itemModelReference,
                oldModelReference.getAllowableValues(),
                oldModelReference.isMap());

        return Optional.of(modelRef);
    }

}