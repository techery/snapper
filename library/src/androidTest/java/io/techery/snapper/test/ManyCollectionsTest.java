package io.techery.snapper.test;

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.innahema.collections.query.functions.Function2;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import io.techery.snapper.BaseSyncTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.dataset.DataSetJoin;
import io.techery.snapper.dataset.DataSetJoin.JoinBuilder;
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
        userStorage.insert(new User("1", 20, 2));
        userStorage.insert(new User("10", 30, 2));
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

    @Test public void join() {
        DataSetJoin<Company, User, Pair<Company, User>> companyUserJoin =
                new JoinBuilder<>(companyStorage, userStorage)
                        .setJoinFunction(new Function2<Company, User, Boolean>() {
                            @Override public Boolean apply(Company company, User user) {
                                return company.getId() == user.getCompanyId();
                            }
                        })
                        .setMapFunction(new Function2<Company, List<User>, Pair<Company, User>>() {
                            @Override public Pair<Company, User> apply(Company company, List<User> users) {
                                User user = users.isEmpty() ? null : users.get(0);
                                return new Pair<>(company, user);
                            }
                        }).create();
        int companyId = 2;
        User user = new User("1", 20, companyId);
        Company company = new Company(companyId, "SmallCo", null);
        assertThat(companyUserJoin.toList(), hasItem(new Pair<>(company, user)));
        assertThat(companyUserJoin.size(), is(2));
    }

    @Test public void closeAll() {
        db.close();
        assertTrue(userStorage.isClosed());
        assertTrue(companyStorage.isClosed());
    }

}