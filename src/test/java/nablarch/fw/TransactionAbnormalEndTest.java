package nablarch.fw;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import nablarch.fw.results.TransactionAbnormalEnd;

import org.junit.Test;


/**
 * {@link TransactionAbnormalEnd}のテストクラス。
 *
 * @author hisaaki sioiri
 */
public class TransactionAbnormalEndTest {

    /** {@link TransactionAbnormalEnd#TransactionAbnormalEnd(int, String, Object...)}のテスト */
    @Test
    public void testConstructor1() {
        TransactionAbnormalEnd exception = new TransactionAbnormalEnd(100, "msgid", "option");

        assertThat("メッセージの確認", exception.getMessage(),
                containsString("An error happened with messageId = [msgid]"));
        assertThat("メッセージID", exception.getMessageId(), is("msgid"));
        assertThat("メッセージオプション", exception.getMessageParams(), is(
                new Object[]{"option"}));
    }

    /** {@link TransactionAbnormalEnd#TransactionAbnormalEnd(int, Throwable, String, Object...)}のテスト。 */
    @Test
    public void testConstructor2() {

        NullPointerException nullPon = new NullPointerException();

        TransactionAbnormalEnd exception = new TransactionAbnormalEnd(
                111, nullPon, "msgid",
                "option1", "option2", "option3"
        );

        assertThat("終了コードは、111", exception.getStatusCode(), is(111));
        assertThat("メッセージID", exception.getMessageId(), is("msgid"));
        assertThat("メッセージオプション", exception.getMessageParams(), is(
                new Object[]{"option1", "option2", "option3"}));

        assertThat("メッセージの確認", exception.getMessage(),
                containsString("An error happened with messageId = [msgid]"));
        assertThat("原因例外はNullPointerExceptionであること。",
                exception.getCause(),
                is((Throwable) nullPon));

    }

    /** 終了コードの設定テスト。 */
    @Test
    public void testInvalidExitCode() {

        try {
            new TransactionAbnormalEnd(99, "");
            fail("does not run.");
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            assertThat("99は範囲外のためエラー", e.getMessage(), containsString(
                    "Exit code was invalid range. Please set it in the range of 199 from 100."));
        }

        assertThat(new TransactionAbnormalEnd(100, "").getStatusCode(), is(100));
    }

}
