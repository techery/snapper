package io.techery.snapper.converter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.io.ByteArrayOutputStream;

public class KryoConverter<T> implements ObjectConverter<T> {

    private final Kryo kryo;
    private final Class<T> className;

    private Input input;
    private Output output;

    public KryoConverter(Class<T> className) {
        this.className = className;
        this.kryo = new Kryo();
        this.kryo.register(className);
        this.kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        //
        this.input = new Input();
        this.output = new Output(new ByteArrayOutputStream());
    }

    @Override
    public byte[] toBytes(T item) {
        kryo.writeObject(output, item);
        output.clear();
        return output.getBuffer();
    }

    @Override
    public T fromBytes(byte[] bytes) {
        input.setBuffer(bytes);
        return kryo.readObject(input, this.className);
    }
}
