package com.weiguangfu.swagger2.plus.util;

import java.util.Objects;

public final class ArrayUtil {

    private ArrayUtil(){}

    public static boolean isEmpty(Object[] objectArray) {
        return Objects.isNull(objectArray) || objectArray.length <= 0;
    }

    public static boolean isNotEmpty(Object[] objectArray) {
        return !isEmpty(objectArray);
    }
}