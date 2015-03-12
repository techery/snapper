package com.example.snapper;

import android.content.Context;

import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.functions.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.nio.ByteBuffer;
import java.util.Comparator;

import io.techery.snapper.DataCollection;
import io.techery.snapper.DroidSnapper;
import io.techery.snapper.dataset.DataSetMap;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.view.DataViewBuilder;
import io.techery.snapper.view.IDataView;

import static org.hamcrest.MatcherAssert.assertThat;

@Config(emulateSdk = 19)
@RunWith(RobolectricTestRunner.class)
public class DroidSnapperTest {

    public static class User implements Indexable {

        private String userId;
        private int age = 10;

        public User() {

        }

        public User(String userId) {
            this.userId = userId;
        }

        public User(String userId, int age) {
            this.userId = userId;
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        @Override
        public ByteBuffer index() {
            return ByteBuffer.wrap(userId.getBytes());
        }
    }

    DataCollection<User> dataCollection;
    IDataView<User> filtered;

    @Before
    public void setup() {
        Context application = RuntimeEnvironment.application.getApplicationContext();
        dataCollection = DroidSnapper.with(application).collection(User.class);

        dataCollection.insert(new User("10000"));
        dataCollection.insert(new User("100"));
        dataCollection.insert(new User("1"));
        dataCollection.insert(new User("10"));
        dataCollection.insert(new User("1000"));

        filtered = dataCollection.view().where(new Predicate<User>() {
            @Override
            public boolean apply(User element) {
                return element.userId.length() > 3;
            }
        }).sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.index().compareTo(o2.index());
            }
        }).build();
    }

    @After
    public void tearDown() {
        dataCollection.clear();
    }

    @Test
    public void testMap() {

        DataSetMap<User, Integer> dataSetMap = new DataSetMap<User, Integer>(dataCollection, new Function1<User, Integer>() {
            @Override
            public Integer apply(User s) {
                return s.userId.length();
            }
        });

        IDataView<Integer> dv = new DataViewBuilder<Integer>(dataSetMap).sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        }).build();

        assertThat("should have 5 elements", dv.size() == 5);
        assertThat("", dv.getItem(0) == 1);
        assertThat("", dv.getItem(1) == 2);
        assertThat("", dv.getItem(2) == 3);
        assertThat("", dv.getItem(3) == 4);
        assertThat("", dv.getItem(4) == 5);

        String item = "12313123";
        dataCollection.insert(new User(item));

        assertThat("should have 6 elements", dv.size() == 6);
    }

    @Test
    public void testSort() {

        IDataView<User> sorted = dataCollection.view().sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.index().compareTo(o2.index());
            }
        }).build();

        assertThat("should have 5 elements", sorted.size() == 5);
        assertThat("", sorted.getItem(0).userId.equalsIgnoreCase("1"));
        assertThat("", sorted.getItem(1).userId.equalsIgnoreCase("10"));
        assertThat("", sorted.getItem(2).userId.equalsIgnoreCase("100"));
        assertThat("", sorted.getItem(3).userId.equalsIgnoreCase("1000"));
        assertThat("", sorted.getItem(4).userId.equalsIgnoreCase("10000"));
    }

    @Test
    public void testWhere() {
        IDataView<User> filtered = dataCollection.view().where(new Predicate<User>() {
            @Override
            public boolean apply(User element) {
                return element.userId.length() > 3;
            }
        }).build();

        assertThat("should have 2 elements", filtered.size() == 2);
    }

    @Test
    public void testAdd() {
        dataCollection.insert(new User("100000"));

        assertThat("should have 2 elements", filtered.size() == 3);
        assertThat("", filtered.getItem(0).userId.equalsIgnoreCase("1000"));
        assertThat("", filtered.getItem(1).userId.equalsIgnoreCase("10000"));
        assertThat("", filtered.getItem(2).userId.equalsIgnoreCase("100000"));
    }

    @Test
    public void testRemove() {

        dataCollection.remove(new User("1000"));

        assertThat("should have 2 elements", filtered.size() == 1);
        assertThat("", filtered.getItem(0).userId.equalsIgnoreCase("10000"));
    }

    @Test
    public void testUpdate() {

        IDataView<User> filtered = dataCollection.view().where(new Predicate<User>() {
            @Override
            public boolean apply(User element) {
                return element.getAge() <= 10 && element.userId.length() > 3;
            }
        }).sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.index().compareTo(o2.index());
            }
        }).build();

        dataCollection.insert(new User("1000", 5));

        assertThat("should have 2 elements", filtered.size() == 2);
        assertThat("", filtered.getItem(0).userId.equalsIgnoreCase("1000"));
        assertThat("", filtered.getItem(1).userId.equalsIgnoreCase("10000"));

        dataCollection.insert(new User("1000", 400));

        assertThat("should have 1 elements", filtered.size() == 1);
        assertThat("", filtered.getItem(0).userId.equalsIgnoreCase("10000"));
    }
}