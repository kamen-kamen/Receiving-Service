package com.waregang.receiving_service.fixtures.utils;

import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;

public class EntityTestUtils {
    public static <T> T withId(T entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
