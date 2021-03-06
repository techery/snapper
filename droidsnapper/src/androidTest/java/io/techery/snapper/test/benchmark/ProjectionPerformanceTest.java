package io.techery.snapper.test.benchmark;

import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.techery.snapper.model.User;
import io.techery.snapper.projection.IProjection;

@RunWith(AndroidJUnit4.class)
public class ProjectionPerformanceTest extends PerformanceTest {

    List<IProjection> projections;
    static final int DATA_VIEW_COUNT = 5;

    static final String DATA_LISTENER_LOG_TAG = "Projection";

    @Override public void initStorage() {
        super.initStorage();
        addProjections(true);
    }

    @Override public void release() {
        for (IProjection projection : projections) {
            projection.close();
        }
        projections.clear();
        super.release();
    }

    private void addProjections(final boolean withInitializerListener) {
        runWithAwait(DATA_VIEW_COUNT, new Runnable() {
            @Override public void run() {
                projections = new ArrayList<IProjection>();
                for (int i = 0; i < DATA_VIEW_COUNT; i++) {
                    IProjection<User> projection = userStorage.projection().build();
                    projection.addDataListener(new MeterDataListener<User>(DATA_LISTENER_LOG_TAG));
                    if (withInitializerListener) {
                        projection.addDataListener(new InitializeAndResumeListener<User>(projection));
                    }
                    projections.add(projection);
                }
            }
        });
    }

    @Override protected void runBatchInsert(List<User> users) {
        expectResumes(DATA_VIEW_COUNT);
        super.runBatchInsert(users);
    }

    @Override protected void runBatchRemove() {
        expectResumes(DATA_VIEW_COUNT);
        super.runBatchRemove();
    }

    @Override protected void runBatchLoad() {
        projections.clear();
        addProjections(false);
        super.runBatchLoad();
    }

}
