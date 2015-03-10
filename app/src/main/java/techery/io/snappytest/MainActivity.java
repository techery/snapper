package techery.io.snappytest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;

import com.innahema.collections.query.functions.Predicate;
import com.snappydb.SnappydbException;

import java.nio.ByteBuffer;
import java.util.Comparator;

import io.techery.snapper.DataCollection;
import io.techery.snapper.DroidSnapper;
import io.techery.snapper.listadapter.DataViewListAdapter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.view.IDataView;


public class MainActivity extends ActionBarActivity {

    int lastId = 400;
    IDataView<User> sortedView;
    DataViewListAdapter<User> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DataCollection<User> dataCollection = DroidSnapper.get(this).collection(User.class);

        // int idx = 300;
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));
        // dataCollection.insert(genUser(idx++));

        // dataCollection.clear();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // List<User> users = new ArrayList<>();
                for (int i = 0; i < 120; i++) {
                    dataCollection.insert(genUser(i));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                // dataCollection.insertAll(users);
            }
        }
        );
        // thread.start();

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

        itemsAdapter = new DataViewListAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, sortedView);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(itemsAdapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        findViewById(R.id.loadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataCollection.insert(genUser(lastId++));
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try {
            DroidSnapper.dbFactory.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // User model
    ///////////////////////////////////////////////////////////////////////////

    public static User genUser(int i) {
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

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    '}';
        }
    }
}
