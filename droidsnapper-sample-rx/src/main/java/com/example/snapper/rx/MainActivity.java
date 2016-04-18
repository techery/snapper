package com.example.snapper.rx;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.snapper.rx.util.RxSnapperListener;
import com.example.snapper.rx.util.SnapperChangeFilter;
import com.example.snapper.rx.util.SnapperChangeSort;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.techery.snapper.datacollection.DataCollection;
import io.techery.snapper.droidsnapper.DroidSnapper;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.projection.IProjection;
import io.techery.snapper.storage.StorageChange;
import rx.Observable;
import rx.subjects.ReplaySubject;


public class MainActivity extends AppCompatActivity {

    DataCollection<User> dataCollection;
    IProjection<User> projection;
    //
    ReplaySubject<Boolean> filteringSignal = ReplaySubject.createWithSize(1);
    volatile int lastId;
    //
    static final int BATCH_PUT_COUNT = 10000;

    @Bind(R.id.listView)
    RecyclerView recyclerView;
    UserAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        dataCollection = DroidSnapper.with(this).collection(User.class);
        projection = dataCollection.projection().build();
        //
        RxSnapperListener<User> snapperListener = new RxSnapperListener<>();
        projection.addDataListener(snapperListener);

        boolean filter;
        if (savedInstanceState == null) filter = false;
        else filter = savedInstanceState.getBoolean("filter");
        filteringSignal.onNext(filter);

        Observable<Pair<List<User>, StorageChange<User>>> source =
                filteringSignal.asObservable()
                        .flatMap(filterOn -> snapperListener.observable()
                                .compose(new SnapperChangeSort<>((u1, u2) -> {
                                    if (u1.id < u2.id) return 1;
                                    else if (u1.id > u2.id) return -1;
                                    else return 0;
                                }))
                                .doOnNext(pair -> {
                                    List<User> items = pair.first;
                                    if (items.isEmpty()) lastId = 0;
                                    else lastId = items.get(0).id;
                                })
                                .compose(new SnapperChangeFilter<>(user -> {
                                    if (!filterOn) return true;
                                    else return user.id % 10 == 0;
                                }))
                        );
        usersAdapter = new UserAdapter(this, source);
        recyclerView.setAdapter(usersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("filter", filteringSignal.getValue());
    }

    @Override
    protected void onDestroy() {
        projection.close();
        super.onDestroy();
    }

    @OnClick(R.id.add)
    void onAddClick() {
        dataCollection.insert(genUser(lastId + 1));
    }

    @OnClick(R.id.add_many)
    void onAddManyClick() {
        List<User> users = new ArrayList<>();
        int lastId = this.lastId + 1;
        for (int i = 0; i < BATCH_PUT_COUNT; i++) {
            users.add(genUser(lastId + i));
        }
        dataCollection.insertAll(users);
    }

    @OnClick(R.id.filter)
    void onFilterClick() {
        filteringSignal.onNext(!filteringSignal.getValue());
    }

    @OnClick(R.id.clear)
    void onClearClick() {
        dataCollection.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    static class UserAdapter extends SnapperRxListAdapter<User, UserAdapter.ViewHolder> {

        public UserAdapter(Context context, Observable<Pair<List<User>, StorageChange<User>>> source) {
            super(context, source);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View itemView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User user = getItem(position);
            ((TextView) holder.itemView).setText(String.valueOf(user.getId()));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
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
        public byte[] index() {
            return ByteBuffer.allocate(4).putInt(id).array();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            return id == user.id;

        }

        @Override
        public int hashCode() {
            return id;
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
