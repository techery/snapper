package io.techery.snapper.test;

import android.support.test.runner.AndroidJUnit4;

import com.innahema.collections.query.functions.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.techery.snapper.BaseSyncTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.dataset.IDataSet.DataListener;
import io.techery.snapper.model.User;
import io.techery.snapper.projection.IProjection;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.util.Waiter;
import io.techery.snapper.util.android.MainThreadDataListener;
import io.techery.snapper.util.android.SingleItemDataListener;
import io.techery.snapper.util.android.SingleItemDataListener.ChangeStatus;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class HelpersTest extends BaseSyncTestCase {

    private DataCollection<User> dataCollection;

    @Before
    public void createCollection() {
        dataCollection = db.collection(User.class);
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
    public void mainThreadListener() throws Throwable {
        final Waiter waiter = new Waiter();
        dataCollection.insert(new User("1"));
        final boolean[] updatedOnMainThread = new boolean[1];
        final User[] user = new User[1];
        waiter.expectResumes(1);
        dataCollection.addDataListener(new MainThreadDataListener<User>(new DataListener<User>() {
            @Override public void onDataUpdated(List<User> items, StorageChange<User> change) {
                updatedOnMainThread[0] = Thread.currentThread().getName().contains("main");
                user[0] = items.isEmpty() ? null : items.get(0);
                if (!items.isEmpty()) waiter.resume();
            }
        }));
        waiter.await();
        assertTrue(updatedOnMainThread[0]);
        assertThat(user[0].getUserId(), is("1"));
    }

    @Test
    public void singleItemListener() {
        dataCollection.insert(new User("10000"));
        dataCollection.insert(new User("100"));
        dataCollection.insert(new User("1"));
        //
        final User[] userToWatch = {null};
        final ChangeStatus[] changeStatus = new ChangeStatus[1];
        IProjection<User> projection = dataCollection.projection()
                .where(new Predicate<User>() {
                    @Override public boolean apply(User element) {
                        return element.getUserId().equals("10000");
                    }
                }).build();
        projection.addDataListener(new SingleItemDataListener<User>() {
            @Override protected void onItemUpdated(User user, ChangeStatus status) {
                userToWatch[0] = user;
                changeStatus[0] = status;
            }
        });
        // creation
        assertThat(userToWatch[0].getUserId(), is("10000"));
        assertThat(userToWatch[0].getAge(), is(100));
        assertThat(changeStatus[0], is(ChangeStatus.ADDED));
        // update
        dataCollection.insert(new User("10000", 50));
        assertThat(userToWatch[0].getAge(), is(50));
        assertThat(changeStatus[0], is(ChangeStatus.UPDATED));
        // remove
        dataCollection.remove(new User("10000", 50));
        assertThat(userToWatch[0], nullValue());
        assertThat(changeStatus[0], is(ChangeStatus.REMOVED));
    }
}
