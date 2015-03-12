package techery.io.snappytest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import com.innahema.collections.query.functions.Predicate;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.techery.snapper.DataCollection;
import io.techery.snapper.DroidSnapper;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.listadapter.DataViewListAdapter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.view.IDataView;


public class MainActivity extends ActionBarActivity {

    DataCollection<User> dataCollection;
    IDataView<User> sortedView;
    DataViewListAdapter<User> itemsAdapter;
    volatile int lastId;

    static final int BATCH_PUT_COUNT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        dataCollection = DroidSnapper.with(this).collection(User.class);

        sortedView = dataCollection.view().sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.id < o2.id) {
                    return 1;
                } else if (o1.id > o2.id) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }).build();

        sortedView.addListener(new IDataSet.Listener<User>() {
            @Override
            public void onDataSetUpdated(IDataSet<User> dataSet, StorageChange<User> change) {
                if (dataSet.iterator().hasNext()) {
                    lastId = dataSet.iterator().next().getValue().id;
                } else {
                    lastId = 0;
                }
            }
        });

        itemsAdapter = new DataViewListAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, sortedView);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(itemsAdapter);
    }

    @Override protected void onDestroy() {
        sortedView.close();
        super.onDestroy();
    }

    @OnClick(R.id.add) void onAddClick() {
        dataCollection.insert(genUser(lastId + 1));
    }

    @OnClick(R.id.add_many) void onAddManyClick() {
        List<User> users = new ArrayList<>();
        int lastId = this.lastId + 1;
        for (int i = 0; i < BATCH_PUT_COUNT; i++) {
            users.add(genUser(lastId + i));
        }
        dataCollection.insertAll(users);
    }

    @OnClick(R.id.filter) void onFilterClick() {
        if (itemsAdapter.getDataView().equals(sortedView)) {
            itemsAdapter.setDataView(sortedView.view()
                            .where(new Predicate<User>() {
                                @Override
                                public boolean apply(User element) {
                                    return element.id % 10 == 0;
                                }
                            })
                            .build()
            );
        } else {
            itemsAdapter.setDataView(sortedView);
        }
    }

    @OnClick(R.id.clear) void onClearClick() {
        dataCollection.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // User model
    ///////////////////////////////////////////////////////////////////////////

    static User genUser(int i) {
        User u = new User(i);
        u.email = String.valueOf(Math.random() * 100);
        u.name = String.valueOf(Math.random() * 100);
        u.price = (float) Math.random();
        u.isActive = Math.random() % 2 == 0;
        return u;
    }

    public static class User implements Indexable {
        public int id;
        public String name;
        public float price;
        public String email;

        public boolean isActive;

        public User() {
        }

        public User(int id) {
            this.id = id;
        }

        @Override
        public ByteBuffer index() {
            return ByteBuffer.allocate(4).putInt(id);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setIsActive(boolean isActive) {
            this.isActive = isActive;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    '}';
        }

    }
}
