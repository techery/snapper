package com.example.snapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

import io.techery.snapper.DataCollection;
import io.techery.snapper.Snapper;
import io.techery.snapper.dataset.DataSetMap;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.view.DataViewBuilder;
import io.techery.snapper.view.IDataView;

import static io.techery.snapper.DroidSnapper.SnapperBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(AndroidJUnit4.class)
public class DroidSnapperTest extends ApplicationTestCase<App> {

    public DroidSnapperTest() {
        super(App.class);
    }

    DataCollection<User> dataCollection;
    IDataView<User> filtered;

    @Before
    public void setup() throws Exception {
        super.setUp();
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //
        SnapperBuilder snapperBuilder = new SnapperBuilder(context);
        Snapper snapper = snapperBuilder.componentFactory(
                new SnappyComponentFactory(snapperBuilder.useDefaultDatabaseFactory("testing_snappydb")) {

                    Executor executor = new Executor() {
                        @Override public void execute(Runnable command) {
                            command.run();
                        }
                    };

                    @Override public Executor createStorageExecutor() {
                        return executor;
                    }

                    @Override public Executor createCollectionExecutor() {
                        return executor;
                    }
                }
        ).build();
        dataCollection = snapper.collection(User.class);

        dataCollection.insert(new User("10000"));
        dataCollection.insert(new User("100"));
        dataCollection.insert(new User("1"));
        dataCollection.insert(new User("10"));
        dataCollection.insert(new User("1000"));

        filtered = dataCollection.view()
                .where(new Predicate<User>() {
                    @Override
                    public boolean apply(User element) {
                        return element.userId.length() > 3;
                    }
                }).sort(new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        return o1.userId.compareTo(o2.userId);
                    }
                })
                .build();
    }

    @After
    public void tearDown() throws Exception {
        dataCollection.clear();
        super.tearDown();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void testWhere() {
        IDataView<User> filtered = dataCollection.view().where(new Predicate<User>() {
            @Override
            public boolean apply(User element) {
                return element.userId.length() > 3;
            }
        }).build();

        assertThat("Filtered DataView should have 2 elements with length > 3", filtered.size(), is(2));
    }

    @Test
    public void testSort() {
        IDataView<User> sorted = dataCollection.view().sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.userId.compareTo(o2.userId);
            }
        }).build();

        assertThat("Sorted DataView should have 5 elements", sorted.size(), is(5));
        assertThat(sorted.getItem(0).userId, equalTo("1"));
        assertThat(sorted.getItem(1).userId, equalTo("10"));
        assertThat(sorted.getItem(2).userId, equalTo("100"));
        assertThat(sorted.getItem(3).userId, equalTo("1000"));
        assertThat(sorted.getItem(4).userId, equalTo("10000"));
    }

    @Test
    public void testAdd() {
        dataCollection.insert(new User("100000"));

        assertThat("Filtered DataView should have 3 elements with length > 3", filtered.size(), is(3));
        List<String> ids = extractUserIds(filtered);
        assertThat(ids, hasItem("1000"));
        assertThat(ids, hasItem("10000"));
        assertThat(ids, hasItem("100000"));
    }

    @Test
    public void testRemove() {
        dataCollection.remove(new User("1000"));

        assertThat("should have 2 elements", filtered.size(), is(1));
        assertThat(filtered.getItem(0).userId, equalTo("10000"));
    }

    @Test
    public void testUpdate() {
        IDataView<User> filtered = dataCollection.view()
                .where(new Predicate<User>() {
                    @Override
                    public boolean apply(User element) {
                        return element.getAge() > 100 && element.userId.length() > 3;
                    }
                }).sort(new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        return o1.userId.compareTo(o2.userId);
                    }
                })
                .build();

        String userId = "1000";
        List<String> ids;

        dataCollection.insert(new User(userId, 10));
        ids = extractUserIds(filtered);
        assertThat("Filtered DataView should have 0 elements with age > 100", filtered.size(), is(0));

        dataCollection.insert(new User(userId, 400));
        ids = extractUserIds(filtered);
        assertThat("Filtered DataView should have 1 elements with age > 100", filtered.size(), is(1));
        assertThat(ids, hasItem(userId));
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

        assertThat("should have 5 elements", dv.size(), is(5));
        assertThat(dv.getItem(0), is(1));
        assertThat(dv.getItem(1), is(2));
        assertThat(dv.getItem(2), is(3));
        assertThat(dv.getItem(3), is(4));
        assertThat(dv.getItem(4), is(5));

        String item = "12313123";
        dataCollection.insert(new User(item));

        assertThat("should have 5 elements", dv.size(), is(6));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private List<String> extractUserIds(Iterable<ItemRef<User>> source) {
        return Queryable.from(source).map(new Converter<ItemRef<User>, String>() {
            @Override public String convert(ItemRef<User> element) {
                return element.getValue().userId;
            }
        }).toList();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Models
    ///////////////////////////////////////////////////////////////////////////

    static class User implements Indexable {

        private String userId;
        private int age = 100;

        public User() {
        }

        public User(String userId) {
            this.userId = userId;
        }

        public User(String userId, int age) {
            this.userId = userId;
            this.age = age;
        }

        @Override
        public byte[] index() {
            return userId.getBytes();
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}