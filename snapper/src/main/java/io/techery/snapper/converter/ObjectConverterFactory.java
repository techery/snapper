package io.techery.snapper.converter;

public interface ObjectConverterFactory {
    <T> ObjectConverter<T> createConverter(Class<T> clazz);
}
