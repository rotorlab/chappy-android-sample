package com.flamebase.database.types;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures all the common/shared logic between the old, ({@link MapTypeAdapter}, and
 * the new, {@link MapAsArrayTypeAdapter}, map type adapters.
 *
 * @author Joel Leitch
 */
abstract class BaseMapTypeAdapter implements JsonSerializer<Map<?, ?>>, JsonDeserializer<Map<?, ?>> {

    protected static final JsonElement serialize(JsonSerializationContext context, Object src, Type srcType) {
        return context.serialize(src, srcType);
    }

    protected static final Map<Object, Object> constructMapType(Type mapType, JsonDeserializationContext context) {
        return new HashMap<>();
    }
}

