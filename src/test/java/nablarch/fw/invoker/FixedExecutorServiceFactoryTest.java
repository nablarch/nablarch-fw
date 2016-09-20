package nablarch.fw.invoker;

import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * {@link FixedExecutorServiceFactoryTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class FixedExecutorServiceFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    FixedExecutorServiceFactory sut = new FixedExecutorServiceFactory();

    @Before
    public void setUp() {
        sut.setThreadPoolSize(1);
        sut.setTimeoutSecond(1L);
    }

    @After
    public void tearDown() {
        sut.startShutdownService();
        sut.shutdownService();
    }
    @Test
    public void testGetExecutorService() {
        sut.initialize();
        ExecutorService executorService = sut.getExecutorService();
        assertThat(executorService, is(not(nullValue())));
    }

    @Test
    public void testStartShutdownTwice() {
        sut.initialize();
        ExecutorService executorService = sut.getExecutorService();
        sut.startShutdownService();
        sut.startShutdownService(); // 2回めは何もしない
    }

    @Test
    public void testShutdown() {
        sut.initialize();
        ExecutorService executorService = sut.getExecutorService();
        sut.shutdownService();
    }

    @Test
    public void testAwaitTerminationInterrupted() {
        sut = new FixedExecutorServiceFactory() {
            @Override
            protected boolean awaitTermination() throws InterruptedException {
                throw new InterruptedException("for test");
            }
        };
        sut.initialize();
        sut.shutdownService();
    }

    @Test
    public void testInitializeFail() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("executorService not initialized.");

        sut = new FixedExecutorServiceFactory() {
            @Override
            protected ExecutorService createExecutorService() {
                return null;
            }
        };
        sut.initialize();
    }
}