package cn.weiguangfu.swagger2.plus.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import springfox.documentation.service.Operation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

public final class ObjectUtil {
    private static Logger logger = LoggerFactory.getLogger(ObjectUtil.class);

    private ObjectUtil(){}

    public static void setFieldValue(Object object, String fieldName, Object fieldValue){
        if (Objects.isNull(object) || StringUtils.isEmpty(fieldName)) {
            return;
        }

        Class<?> suppclass = object.getClass();
        do {
            try {
                Field[] parameterList = suppclass.getDeclaredFields();
                if (ArrayUtil.isNotEmpty(parameterList)) {
                    for (Field parameter : parameterList) {
                        if (Objects.equals(fieldName, parameter.getName())) {
                            parameter.setAccessible(true);
                            parameter.set(object, fieldValue);
                            break;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                logger.debug("ObjectUtil setFieldValue error, fieldName=" + fieldName
                        + ", fieldValue=" + fieldValue + ", error: " + e.getMessage());
            }
            suppclass = suppclass.getSuperclass();
        } while (Objects.nonNull(suppclass));
    }

    public static <T extends Annotation> T getFieldAnnotation(Class<?> clazz, String fieldName, Class<T> annotationClass) {
        if (Objects.isNull(clazz)) {
            return null;
        }

        do {
            Field[] parameterList = clazz.getDeclaredFields();
            if (ArrayUtil.isNotEmpty(parameterList)) {
                for (Field parameter : parameterList) {
                    if (Objects.equals(fieldName, parameter.getName())) {
                        return parameter.getAnnotation(annotationClass);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (Objects.nonNull(clazz));

        return null;
    }

    /**
     * 双数组查找是否有其中单个元素相等
     * @param sourceArray 源数组
     * @param targetArray 目标数组
     * @return true:双列表有其中一个元素相等, false:双列表没有一个元素相等
     */
    public static boolean isSingleEquals(Object[] sourceArray, Object[] targetArray) {
        if (ArrayUtil.isEmpty(sourceArray) || ArrayUtil.isEmpty(targetArray)) {
            return false;
        }
        for (Object sourceObject : sourceArray) {
            for (Object targetObject : targetArray) {
                if (Objects.equals(sourceObject, targetObject)) {
                    return true;
                }
            }
        }
        return false;
    }
}