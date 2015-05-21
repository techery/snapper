package io.techery.snapper.util;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.queriables.Queryable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.techery.snapper.model.ItemRef;
import io.techery.snapper.model.User;

public class ModelUtil {

    private ModelUtil(){}

    public static List<User> generateUsers(int batchSize) {
        List<User> users = new ArrayList<User>();
        Random randomSource = new Random(batchSize);
        for (int i = 0; i < batchSize; i++) {
            int random = randomSource.nextInt();
            users.add(new User(String.valueOf(i), random / 2));
        }
        return users;
    }

    public static List<String> extractUserIds(Iterable<ItemRef<User>> source) {
        return Queryable.from(source).map(new Converter<ItemRef<User>, String>() {
            @Override public String convert(ItemRef<User> element) {
                return element.getValue().getUserId();
            }
        }).toList();
    }

    public static List<Integer> extractAges(Iterable<ItemRef<User>> source) {
        return Queryable.from(source).map(new Converter<ItemRef<User>, Integer>() {
            @Override public Integer convert(ItemRef<User> element) {
                return element.getValue().getAge();
            }
        }).toList();
    }
}
