package io.techery.snapper.test.benchmark;

import java.util.ArrayList;
import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.User;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.projection.IProjection;

public class ProjectionSpeedTest extends SpeedTest {

    List<IProjection> projections;
    static final int DATA_VIEW_COUNT = 5;

    @Override public void initStorage() {
        super.initStorage();
        projections = new ArrayList<>();
        for (int i = 0; i < DATA_VIEW_COUNT; i++) {
            IProjection<User> view = userStorage.projection().build();
            view.addDataListener(new IDataSet.DataListener<User>() {
                @Override public void onDataUpdated(List<User> items, StorageChange<User> change) {
                    if (canMeterChange(change)) METER.beat("View updated with " + change);
                }
            });
            projections.add(view);
        }
    }

    @Override public void release() {
        super.release();
        for (IProjection view : projections) {
            view.close();
        }
        projections.clear();
    }
}
