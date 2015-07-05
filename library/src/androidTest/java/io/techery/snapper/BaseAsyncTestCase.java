package io.techery.snapper;


import net.jodah.concurrentunit.Waiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import io.techery.snapper.snappydb.SnappyComponentFactory.DefaultSnappyComponentFactory;
import timber.log.Timber;

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
    private AtomicInteger expectedResumes = new AtomicInteger();

    protected void expectResumes(int count) {
        expectedResumes.addAndGet(count);
    }

    protected void await() {
        try {
            waiter.await(0, expectedResumes.get());
        } catch (Throwable throwable) {
            Timber.w(throwable, "Can't wait :(");
        }
    }

    protected void resume() {
        expectedResumes.decrementAndGet();
        waiter.resume();
    }

    protected void runWithAwait(int resumes, Runnable command) {
        expectResumes(resumes);
        command.run();
        await();
    }

}
