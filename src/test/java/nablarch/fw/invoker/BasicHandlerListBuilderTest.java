package nablarch.fw.invoker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nablarch.fw.Handler;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link BasicHandlerListBuilderTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class BasicHandlerListBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BasicHandlerListBuilder<Object> sut;

    private List<Handler<?, ?>> handlerList;

    @Before
    public void setUp() {
        sut = new BasicHandlerListBuilder<Object>();
        handlerList = new ArrayList<Handler<?, ?>>();
        sut.setHandlerList(handlerList);
    }

    @Test
    public void testGetHandlerListWithoutProtection() {
        sut.setProtectMode("NONE");
        Object unusedArg = new Object();
        assertThat(sut.getHandlerList(unusedArg), is(sameInstance(handlerList)));
    }

    @Test
    public void testGetHandlerListWithCopy() {
        sut.setProtectMode("COPY");

        Object unusedArg = new Object();
        List<Handler<?, ?>> actual = sut.getHandlerList(unusedArg);
        assertThat(actual, is(handlerList));
        assertThat(actual, not(sameInstance(handlerList)));
        actual.add(new DummyHandler());  // OK
    }

    @Test
    public void testGetHandlerListWithUnmodifiable() {
        sut.setProtectMode("UNMODIFIABLE");
        Object unusedArg = new Object();
        List<Handler<?, ?>> actual = sut.getHandlerList(unusedArg);
        assertThat(actual, is(handlerList));
        assertThat(actual, not(sameInstance(handlerList)));
        try {
            actual.add(new DummyHandler());  // NG
            fail();
        } catch (UnsupportedOperationException e) {
            // NOP
        }
    }

    @Test
    public void testGetNullHandlerList() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("handlerList must be set.");

        sut.setHandlerList(null);
        Object unusedArg = new Object();
        sut.getHandlerList(unusedArg);
    }

}