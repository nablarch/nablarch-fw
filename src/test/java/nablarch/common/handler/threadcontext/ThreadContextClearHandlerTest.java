package nablarch.common.handler.threadcontext;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import nablarch.core.ThreadContext;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

import org.junit.Before;
import org.junit.Test;

/**
 * {@link ThreadContextClearHandler}のテスト。
 */
public class ThreadContextClearHandlerTest {

    private final ThreadContextClearHandler sut = new ThreadContextClearHandler();

    private final ExecutionContext context = new ExecutionContext();

    @Before
    public void setUp() throws Exception {
        final ThreadContextHandler threadContextHandler = new ThreadContextHandler();
        threadContextHandler.setAttributes(Collections.<ThreadContextAttribute>singletonList(
                new ThreadContextAttribute() {
                    @Override
                    public String getKey() {
                        return "test-key";
                    }

                    @Override
                    public Object getValue(final Object req, final ExecutionContext ctx) {
                        return "value";
                    }
                }));
        context.addHandler(threadContextHandler);
    }

    @Test
    public void ハンドラ終了後にThreadContextがクリアされていること() throws Exception {
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(final Object o, final ExecutionContext context) {
                assertThat("ハンドラ内ではスレッドコンテキストを参照できる", (String) ThreadContext.getObject("test-key"), is("value"));
                return "ok";
            }
        });
        sut.handle("input", context);

        assertThat("削除されていること", ThreadContext.getObject("test-key"), is(nullValue()));
    }

    @Test
    public void 後続のハンドラで例外が発生した場合でもThreadContextがクリアされていること() throws Exception {
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(final Object o, final ExecutionContext context) {
                assertThat("ハンドラ内ではスレッドコンテキストを参照できる", (String) ThreadContext.getObject("test-key"), is("value"));
                throw new IllegalStateException("おかしいよん");
            }
        });
        try {
            sut.handle("input", context);
        } catch (IllegalStateException ignored) {
        }

        assertThat("削除されていること", ThreadContext.getObject("test-key"), is(nullValue()));
    }
}