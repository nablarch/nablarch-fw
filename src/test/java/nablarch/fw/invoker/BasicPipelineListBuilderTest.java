package nablarch.fw.invoker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link BasicPipelineListBuilder}のテスト。
 *
 * @author T.Kawasaki
 */
public class BasicPipelineListBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    BasicPipelineListBuilder sut = new BasicPipelineListBuilder();

    /** ハンドラリストの設定ができること。*/
    @Test
    public void testSetHandlerList() {
        List<Object> handlerList = new ArrayList<Object>();
        sut.setHandlerList(handlerList);
        assertThat(sut.getHandlerList(), is(handlerList));
    }

    /** 保護モードの設定ができること。*/
    @Test
    public void testSetProtector() {
        sut.setProtectMode("COPY");
        List<Object> orig = new ArrayList<Object>();
        sut.setHandlerList(orig);
        List<Object> actual = sut.getHandlerList();
        assertThat(actual, is(orig));
        assertThat(actual == orig, is(false));  // COPY
    }

    /**
     * ハンドラリストが設定されていない状態で、
     * ハンドラリストを取得しようとした場合、例外が発生すること
     */
    @Test
    public void testGetHandlerListNull() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("handlerList must be set.");

        sut.getHandlerList();
    }
}