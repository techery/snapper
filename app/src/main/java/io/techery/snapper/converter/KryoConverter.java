package io.techery.snapper.converter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.io.ByteArrayOutputStream;

public class KryoConverter<T> implements ObjectConverter<T> {

    private final Kryo kryo = new Kryo();
    private final Class<T> className;

    public KryoConverter(Class<T> className) {
        this.className = className;
        this.kryo.register(className);
        this.kryo.setAsmEnabled(true);
        this.kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
    }

    @Override
    public byte[] toBytes(T item) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);

        kryo.writeObject(output, item);
        output.close();

        return stream.toByteArray();
    }

    @Override
    public T fromBytes(byte[] bytes) {
        Input input = new Input(bytes);
        T t = kryo.readObject(input, this.className);
        input.close();
        return t;
    }
}
