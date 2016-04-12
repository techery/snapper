package io.techery.snapper.util;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.queriables.Queryable;

import java.util.Collection;
import java.util.List;

public class ListUtils {
    public static <F, T> List<T> map(Collection<F> collection, final Function1<F, T> mapper) {
        return Queryable.from(collection).map(new Converter<F, T>() {
            @Override
            public T convert(F element) {
                return mapper.apply(element);
            }
        }).toList();
    }
}
