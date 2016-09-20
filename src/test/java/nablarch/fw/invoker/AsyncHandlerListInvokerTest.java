package nablarch.fw.invoker;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link AsyncHandlerListInvokerTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class AsyncHandlerListInvokerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    AsyncHandlerListInvoker<Object, String> sut = new AsyncHandlerListInvoker<Object, String>();
    FixedExecutorServiceFactory executorServiceFactory = new FixedExecutorServiceFactory();

    BasicHandlerListInvoker<Object, String> invoker = new BasicHandlerListInvoker<Object, String>();

    BasicHandlerListBuilder<Object> builder = new BasicHandlerListBuilder<Object>();
    @Before
    public void setUp() {
        builder.setHandlerList(Collections.<Handler<?, ?>>singletonList(new StringifyHandler()));
        invoker.setHandlerListBuilder(builder);
        sut.setHandlerListInvoker(invoker);
        executorServiceFactory.initialize();
        sut.setExecutorServiceFactory(executorServiceFactory);
    }

    @Test
    public void testInvokeAsync() throws ExecutionException, InterruptedException {
        Future<String> future = sut.invokeHandlerList(1L, new ExecutionContext());
        assertThat(future.get(), is("1"));
    }

    @Test
    public void testNoInvoker() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("handlerListInvoker must be set.");

        sut.setHandlerListInvoker(null);
        sut.invokeHandlerList(1L, new ExecutionContext());
    }

    @Test
    public void testNoExecutorServiceFactory() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("executorServiceFactory must be set.");

        sut.setExecutorServiceFactory(null);
        sut.invokeHandlerList(1L, new ExecutionContext());
    }

    @After
    public void tearDown() {
        executorServiceFactory.startShutdownService();
        executorServiceFactory.shutdownService();
    }

}