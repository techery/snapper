package io.techery.snapper.converter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;

public class KryoConverter<T> implements ObjectConverter<T> {

    private final Kryo kryo = new Kryo();

    private final Class<T> className;

    public KryoConverter(Class<T> className) {
        this.className = className;
        this.kryo.register(className);
        this.kryo.setAsmEnabled(true);
    }

    @Override
    public byte[] toBytes(T item) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final Output output = new Output(stream);

        this.kryo.writeObject(output, item);
        output.close();

        return stream.toByteArray();
    }

    @Override
    public T fromBytes(byte[] bytes) {
        final Input input = new Input(bytes);
        return this.kryo.readObject(input, this.className);
    }
}
