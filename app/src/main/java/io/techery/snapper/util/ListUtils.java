package io.techery.snapper.util;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.queriables.Queryable;

import java.util.List;

public class ListUtils {
    public static <F, T> List<T> map(List<F> list, final Function1<F,T> mapper) {
        return Queryable.from(list).map(new Converter<F, T>() {
            @Override
            public T convert(F element) {
                return mapper.apply(element);
            }
        }).toList();
    }
}
