package techery.io.snappytest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;

import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.innahema.collections.query.functions.Predicate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.techery.snapper.DataCollection;
import io.techery.snapper.Snapper;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.view.IDataView;


public class MainActivity extends ActionBarActivity {

    public static class Comment {
        String message;

        public Comment() {
        }

        public Comment(String message) {
            this.message = message;
        }
    }

    public static class User implements Indexable {
        public int id;
        public String name;
        public float price;
        public String email;
        public boolean isActive;

        @CollectionSerializer.BindCollection(
                elementClass = Comment.class,
                elementsCanBeNull = false)
        public List<Comment> comments;

        public User(int id) {
            this.id = id;
        }

        public User() {
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

    public User genUser(int i) {

        User u = new User(i);
        u.email = String.valueOf(Math.random() * 100);
        u.name = String.valueOf(Math.random() * 100);
        u.price = (float) Math.random();
        u.isActive = Math.random() % 2 == 0;
        u.comments = new ArrayList<>();
        u.comments.add(new Comment("demo"));

        return u;
    }

    DataCollection<User> dataCollection;
    IDataView<User> sortedView;

    DataViewListAdapter<User> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SnappyComponentFactory componentFactory = new SnappyComponentFactory(this);
        SnapperKeyValueStorageFactory keyValueStorageFactory = new SnapperKeyValueStorageFactory(componentFactory);
        Snapper snapper = new Snapper(keyValueStorageFactory);

        try {
            dataCollection = snapper.createCollection(User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//                    dataCollection.insert(genUser(i + 10));
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sortedView == null) {
                    return;
                }

                if (itemsAdapter.getDataView().equals(sortedView)) {

                    IDataView<User> view = sortedView.view().where(new Predicate<User>() {
                        @Override
                        public boolean apply(User element) {
                            return element.id < 15;
                        }
                    }).build();

                    itemsAdapter.setDataView(view);
                } else {
                    itemsAdapter.setDataView(sortedView);
                }
            }
        });

        findViewById(R.id.loadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortedView = dataCollection.view().sort(new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        if (o1.id < o2.id) {
                            return -1;
                        } else if (o1.id > o2.id) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }).build();

                itemsAdapter = new DataViewListAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, sortedView);

                ListView listView = (ListView) findViewById(R.id.listView);

                listView.setAdapter(itemsAdapter);
            }
        });


    }
}
