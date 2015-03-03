package io.techery.snapper.converter;

public interface ObjectConverter<T> {
    byte[] toBytes(T item);
    T fromBytes(byte[] bytes);
}
