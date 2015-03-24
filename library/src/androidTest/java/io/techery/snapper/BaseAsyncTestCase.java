package io.techery.snapper;


import java.util.concurrent.ExecutorService;

import io.techery.snapper.snappydb.SnappyComponentFactory.DefaultSnappyComponentFactory;
import io.techery.snapper.util.Waiter;

public class BaseAsyncTestCase extends BaseTestCase {

    DefaultSnappyComponentFactory componentFactory = new DefaultSnappyComponentFactory(null);

    @Override public void prepareDb() {
        super.prepareDb();
    }

    @Override protected ExecutorService provideStorageExecutor() {
        return componentFactory.createStorageExecutor();
    }

    @Override protected ExecutorService provideCollectionExecutor() {
        return componentFactory.createCollectionExecutor();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Waiter for concurrency
    ///////////////////////////////////////////////////////////////////////////

    protected Waiter waiter = new Waiter();

    protected void expectResumes(int count) {
        waiter.expectResumes(count);
    }

    protected void await() {
        try {
            waiter.await(0l, waiter.getExpectedResumes());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected void resume() {
        waiter.resume();
    }

    protected void runWithAwait(int resumes, Runnable command) {
        expectResumes(resumes);
        command.run();
        await();
    }

}
