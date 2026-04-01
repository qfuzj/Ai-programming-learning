package com.travel.advisor.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public final class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new IllegalStateException("Bean 拷贝失败", e);
        }
    }

    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        List<T> list = new ArrayList<>();
        if (sourceList == null || sourceList.isEmpty()) {
            return list;
        }
        for (Object source : sourceList) {
            list.add(copy(source, targetClass));
        }
        return list;
    }
}
