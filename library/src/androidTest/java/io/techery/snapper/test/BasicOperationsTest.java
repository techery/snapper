package io.techery.snapper.test;

import android.support.test.runner.AndroidJUnit4;

import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Comparator;
import java.util.List;

import io.techery.snapper.BaseSyncTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.dataset.DataSetMap;
import io.techery.snapper.dataset.IDataSet.StatusListener;
import io.techery.snapper.model.User;
import io.techery.snapper.projection.IProjection;
import io.techery.snapper.projection.ProjectionBuilder;
import io.techery.snapper.util.ModelUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class BasicOperationsTest extends BaseSyncTestCase {

    DataCollection<User> dataCollection;
    IProjection<User> filteredProjection;

    ///////////////////////////////////////////////////////////////////////////
    // Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Before
    public void createCollectionAndFilter() {
        dataCollection = db.collection(User.class);
        filteredProjection = dataCollection.projection()
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
        if (!dataCollection.isClosed()) {
            dataCollection.clear();
            dataCollection.close();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void oneModelManyCollections() {
        dataCollection = db.collection(User.class);
        insertUsers();
        assertThat(dataCollection.size(), is(5));

        dataCollection = db.collection(User.class, "guest");
        assertThat(dataCollection.size(), is(0));
    }

    @Test
    public void loadCollection() {
        dataCollection.insertAll(ModelUtil.generateUsers(1000));
        dataCollection.close();
        dataCollection = db.collection(User.class);
        assertThat(Queryable.from(dataCollection).count(), is(1000));
    }

    @Test
    public void buildFilteredProjection() {
        insertUsers();
        IProjection<User> filtered = dataCollection.projection().where(new Predicate<User>() {
            @Override
            public boolean apply(User element) {
                return element.getUserId().length() > 3;
            }
        }).build();

        assertThat("Filtered Projection should have 2 elements with length > 3", filtered.size(), is(2));
    }

    @Test
    public void buildSortedProjection() {
        insertUsers();
        IProjection<User> sorted = dataCollection.projection().sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getUserId().compareTo(o2.getUserId());
            }
        }).build();

        assertThat("Sorted Projection should have 5 elements", sorted.size(), is(5));
        assertThat(sorted.getItem(0).getUserId(), equalTo("1"));
        assertThat(sorted.getItem(1).getUserId(), equalTo("10"));
        assertThat(sorted.getItem(2).getUserId(), equalTo("100"));
        assertThat(sorted.getItem(3).getUserId(), equalTo("1000"));
        assertThat(sorted.getItem(4).getUserId(), equalTo("10000"));
    }

    @Test
    public void addOne() {
        insertUsers();
        dataCollection.insert(new User("100000"));

        assertThat("Filtered Projection should have 3 elements with length > 3", filteredProjection.size(), is(3));
        List<String> ids = ModelUtil.extractUserIds(filteredProjection);
        assertThat(ids, hasItem("1000"));
        assertThat(ids, hasItem("10000"));
        assertThat(ids, hasItem("100000"));
    }

    @Test
    public void removeOne() {
        insertUsers();
        dataCollection.remove(new User("1000"));

        assertThat("should have 2 elements", filteredProjection.size(), is(1));
        assertThat(filteredProjection.getItem(0).getUserId(), equalTo("10000"));
    }

    @Test
    public void updateOne() {
        insertUsers();
        IProjection<User> filtered = dataCollection.projection()
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

        dataCollection.insert(new User(userId, 10));
        assertThat("Filtered Projection should have 0 elements with age > 100", filtered.size(), is(0));

        dataCollection.insert(new User(userId, 400));
        List<String> ids = ModelUtil.extractUserIds(filtered);
        assertThat("Filtered Projection should have 1 elements with age > 100", filtered.size(), is(1));
        assertThat(ids, hasItem(userId));
    }

    @Test
    public void buildMappedDataSet() {
        insertUsers();
        DataSetMap<User, Integer> dataSetMap = new DataSetMap<User, Integer>(dataCollection, new Function1<User, Integer>() {
            @Override
            public Integer apply(User s) {
                return s.getUserId().length();
            }
        });

        IProjection<Integer> dv = new ProjectionBuilder<Integer>(dataSetMap).sort(new Comparator<Integer>() {
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void closeAll() {
        db.close();
        assertTrue(dataCollection.isClosed());
        assertTrue(filteredProjection.isClosed());
    }

    @Test
    public void collectionAfterClose() {
        db.close();
        thrown.expect(IllegalStateException.class);
        dataCollection.insert(new User("123123"));
    }

    @Test
    public void projectionAfterClose() {
        StatusListener mockListener = mock(StatusListener.class);
        filteredProjection.addStatusListener(mockListener);
        db.close();
        verify(mockListener, only()).onClosed();
        //
        thrown.expect(IllegalStateException.class);
        filteredProjection.toList();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void insertUsers() {
        dataCollection.insert(new User("10000"));
        dataCollection.insert(new User("100"));
        dataCollection.insert(new User("1"));
        dataCollection.insert(new User("10"));
        dataCollection.insert(new User("1000"));
    }

}