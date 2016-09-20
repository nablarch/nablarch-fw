package nablarch.fw.invoker;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link BasicHandlerListInvokerTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class BasicHandlerListInvokerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    BasicHandlerListInvoker<Object, String> sut = new BasicHandlerListInvoker<Object, String>();

    BasicHandlerListBuilder<Object> builder = new BasicHandlerListBuilder<Object>();

    ExecutionContext ctx = new ExecutionContext();

    @Before
    public void setUp() {
        sut.setHandlerListBuilder(builder);
        builder.setHandlerList(Collections.<Handler<?, ?>>singletonList(new StringifyHandler()));

    }

    @Test
    public void testInvoke() {
        String actual = sut.invokeHandlerList(1, ctx);
        assertThat(actual, is("1"));
    }

    @Test
    public void testHandlerListBuilderNotSet() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("handlerListBuilder must be set.");

        sut = new BasicHandlerListInvoker<Object, String>();
        sut.invokeHandlerList(1, ctx);
    }
}