package nablarch.fw.invoker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import nablarch.fw.Handler;
import nablarch.fw.invoker.HandlerListProtector.ProtectMode;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * {@link HandlerListProtectorTest}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class HandlerListProtectorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private List<Handler<?, ?>> origin = new ArrayList<Handler<?, ?>>();
    private HandlerListProtector<Handler<?, ?>> sut = new HandlerListProtector();

    @Test
    public void testNone() {
        sut.setProtectMode(ProtectMode.NONE);
        assertThat(sut.protect(origin), is(sameInstance(origin)));
    }

    @Test
    public void testCopy() {
        sut.setProtectMode(ProtectMode.COPY);
        List<Handler<?, ?>> copied = sut.protect(origin);
        assertThat(copied, not(sameInstance(origin)));
        copied.add(new DummyHandler()); // OK
    }

    @Test
    public void testUnmodifiable() {
        expectedException.expect(UnsupportedOperationException.class);

        sut.setProtectMode(ProtectMode.UNMODIFIABLE);
        List<Handler<?, ?>> sealed = sut.protect(origin);
        assertThat(sealed, not(sameInstance(origin)));
        sealed.add(new DummyHandler()); // NG
    }

    @Test
    public void testNullArg() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("argument must not be null.");

        sut.setProtectModeExpression("COPY"); // just for coverage..
        sut.protect(null);
    }
}