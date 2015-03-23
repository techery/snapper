package io.techery.snapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutorService;

import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.storage.DatabaseFactory;
import io.techery.snapper.util.SimpleExecutorService;

@RunWith(AndroidJUnit4.class)
public class BaseTestCase {

    protected Snapper db;
    protected ExecutorService executor = new SimpleExecutorService() {
        @Override public void execute(Runnable command) {
            command.run();
        }
    };

    @Before
    public void prepareDb() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Create Snapper instance with executors on the same thread as tests
        DroidSnapper.SnapperBuilder snapperBuilder = new DroidSnapper.SnapperBuilder(context);
        DatabaseFactory databaseFactory = snapperBuilder.useDefaultDatabaseFactory("snappydb_test");
        db = snapperBuilder.componentFactory(new SnappyComponentFactory(databaseFactory) {

            @Override public ExecutorService createStorageExecutor() {
                return executor;
            }

            @Override public ExecutorService createCollectionExecutor() {
                return executor;
            }
        }).build();
    }

}
