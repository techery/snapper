package io.techery.snapper.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import io.techery.snapper.converter.ObjectConverter;

import static com.esotericsoftware.kryo.serializers.DefaultSerializers.DateSerializer;

public class KryoConverter<T> implements ObjectConverter<T> {

    private final Kryo kryo;
    private final Class<T> clazz;

    private Input input;
    private Output output;

    public KryoConverter(Class<T> clazz) {
        this.clazz = clazz;
        this.kryo = new Kryo();
        this.kryo.register(clazz);
        this.kryo.register(Date.class, new DateSerializer());
        this.kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        this.kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
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
        input.close();
        input.setBuffer(bytes);
        return kryo.readObject(input, this.clazz);
    }
}
