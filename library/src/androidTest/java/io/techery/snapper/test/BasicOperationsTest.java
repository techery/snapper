package io.techery.snapper.test;

import android.support.test.runner.AndroidJUnit4;

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

import io.techery.snapper.BaseTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.dataset.DataSetMap;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.model.User;
import io.techery.snapper.view.DataViewBuilder;
import io.techery.snapper.view.IDataView;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(AndroidJUnit4.class)
public class BasicOperationsTest extends BaseTestCase {

    DataCollection<User> dataCollection;
    IDataView<User> filtered;

    ///////////////////////////////////////////////////////////////////////////
    // Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Before
    public void createCollectionAndFilter() {
        dataCollection = db.collection(User.class);

        dataCollection.insert(new User("10000"));
        dataCollection.insert(new User("100"));
        dataCollection.insert(new User("1"));
        dataCollection.insert(new User("10"));
        dataCollection.insert(new User("1000"));

        filtered = dataCollection.view()
                .where(new Predicate<User>() {
                    @Override
                    public boolean apply(User element) {
                        return element.getUserId().length() > 3;
                    }
                }).sort(new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        return o1.getUserId().compareTo(o2.getUserId());
                    }
                })
                .build();
    }

    @After
    public void releaseCollection() throws Exception {
        dataCollection.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void testWhere() {
        IDataView<User> filtered = dataCollection.view().where(new Predicate<User>() {
            @Override
            public boolean apply(User element) {
                return element.getUserId().length() > 3;
            }
        }).build();

        assertThat("Filtered DataView should have 2 elements with length > 3", filtered.size(), is(2));
    }

    @Test
    public void testSort() {
        IDataView<User> sorted = dataCollection.view().sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getUserId().compareTo(o2.getUserId());
            }
        }).build();

        assertThat("Sorted DataView should have 5 elements", sorted.size(), is(5));
        assertThat(sorted.getItem(0).getUserId(), equalTo("1"));
        assertThat(sorted.getItem(1).getUserId(), equalTo("10"));
        assertThat(sorted.getItem(2).getUserId(), equalTo("100"));
        assertThat(sorted.getItem(3).getUserId(), equalTo("1000"));
        assertThat(sorted.getItem(4).getUserId(), equalTo("10000"));
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
        assertThat(filtered.getItem(0).getUserId(), equalTo("10000"));
    }

    @Test
    public void testUpdate() {
        IDataView<User> filtered = dataCollection.view()
                .where(new Predicate<User>() {
                    @Override
                    public boolean apply(User element) {
                        return element.getAge() > 100 && element.getUserId().length() > 3;
                    }
                }).sort(new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        return o1.getUserId().compareTo(o2.getUserId());
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
                return s.getUserId().length();
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
                return element.getValue().getUserId();
            }
        }).toList();
    }

}