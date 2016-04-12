package io.techery.snapper.kryo;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.converter.ObjectConverterFactory;

public class KryoConverterFactory implements ObjectConverterFactory {

    @Override
    public <T> ObjectConverter<T> createConverter(Class<T> clazz) {
        return new KryoConverter<T>(clazz);
    }
}
