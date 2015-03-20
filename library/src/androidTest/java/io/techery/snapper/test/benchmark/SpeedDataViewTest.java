package io.techery.snapper.test.benchmark;

import java.util.ArrayList;
import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.User;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.view.IDataView;

public class SpeedDataViewTest extends SpeedTest {

    List<IDataView> views;
    static final int DATA_VIEW_COUNT = 5;

    @Override public void initStorage() {
        super.initStorage();
        views = new ArrayList<>();
        for (int i = 0; i < DATA_VIEW_COUNT; i++) {
            IDataView<User> view = userStorage.view().build();
            view.addListener(new IDataSet.Listener<User>() {
                @Override public void onDataSetUpdated(IDataSet<User> dataSet, StorageChange<User> change) {
                    if (canMeterChange(change)) METER.beat("View updated with " + change);
                }
            });
            views.add(view);
        }
    }

    @Override public void release() {
        super.release();
        for (IDataView view : views) {
            view.close();
        }
        views.clear();
    }
}
