package io.techery.snapper.test;

import android.support.test.runner.AndroidJUnit4;

import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import io.techery.snapper.BaseSyncTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.model.Company;
import io.techery.snapper.model.User;
import io.techery.snapper.projection.IProjection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(AndroidJUnit4.class)
public class ManyCollectionsTest extends BaseSyncTestCase {

    DataCollection<User> userStorage;
    DataCollection<Company> companyStorage;

    ///////////////////////////////////////////////////////////////////////////
    // Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Before public void createCollectionAndFilter() {
        userStorage = db.collection(User.class);
        userStorage.insert(new User("10000"));
        userStorage.insert(new User("100"));
        userStorage.insert(new User("1"));
        userStorage.insert(new User("10"));
        userStorage.insert(new User("1000"));

        companyStorage = db.collection(Company.class);
        companyStorage.insert(new Company(1, "BigCo", Collections.singletonList(new User("100"))));
        companyStorage.insert(new Company(2, "SmallCo", Collections.singletonList(new User("1"))));
    }

    @After public void releaseCollection() throws Exception {
        if (!userStorage.isClosed()) userStorage.clear();
        if (!companyStorage.isClosed()) companyStorage.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests
    ///////////////////////////////////////////////////////////////////////////

    @Test public void storageConsistency() {
        assertThat(userStorage.projection().build().toList().size(), is(5));
        assertThat(companyStorage.projection().build().toList().size(), is(2));
    }

    @Test public void modelEqualityFromDiffStorages() {
        IProjection<Company> smallCoView = companyStorage.projection().where(new Predicate<Company>() {
            @Override public boolean apply(Company element) {
                return element.getName().contains("SmallCo");
            }
        }).build();
        IProjection<User> smallCoUserView = userStorage.projection().where(new Predicate<User>() {
            @Override public boolean apply(User element) {
                return element.getUserId().equals("1");
            }
        }).build();
        Company company = Queryable.from(smallCoView.toList()).first();
        User user = Queryable.from(smallCoUserView.toList()).first();
        assertThat(company.getUsers(), hasItem(user));
    }

    @Test public void closeAll() {
        db.close();
        assertTrue(userStorage.isClosed());
        assertTrue(companyStorage.isClosed());
    }

}