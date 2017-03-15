package nablarch.common.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import org.junit.After;
import org.junit.Test;

/**
 * {@link RequestUtil}のテスト。
 *
 * @author Kiyohito Itoh
 */
public class RequestUtilTest {

    @After
    public void tearDown() throws Throwable {
        SystemRepository.clear();
    }

    /**
     * {@link RequestUtil#getRequestId(String)}のテスト。<br />
     * リポジトリに{@link ShortRequestIdExtractor}が定義されているケース。
     */
    @Test
    public void testGetRequestIdForShort() {

        // リポジトリにShortRequestIdExtractorを定義
        SystemRepository.load(new ObjectLoader() {
            @SuppressWarnings("serial")
            @Override
            public Map<String, Object> load() {
                return new HashMap<String, Object>() {{
                    put("requestIdExtractor", new ShortRequestIdExtractor());
                }};
            }
        });

        assertThat(RequestUtil.getRequestId(" /a/b/cde/f.html "), is("f"));
        assertThat(RequestUtil.getRequestId(null), is(nullValue()));
    }

    /**
     * {@link RequestUtil#getRequestId(String)}のテスト。<br />
     * リポジトリに{@link BasicRequestIdExtractor}が定義されているケース。
     */
    @Test
    public void testGetRequestIdForBasic() {

        // リポジトリにBasicRequestIdExtractorを定義
        SystemRepository.load(new ObjectLoader() {
            @SuppressWarnings("serial")
            @Override
            public Map<String, Object> load() {
                return new HashMap<String, Object>() {{
                    put("requestIdExtractor", new BasicRequestIdExtractor());
                }};
            }
        });

        assertThat(RequestUtil.getRequestId("/a/b/cde?test=aaa#abc"), is("/a/b/cde"));
        assertThat(RequestUtil.getRequestId(null), is(nullValue()));
    }

    /**
     * {@link RequestUtil#getRequestId(String)}のテスト。<br />
     * リポジトリに{@link RequestIdExtractor}の実装クラスが定義されていないケース。
     */
    @Test
    public void testGetRequestIdForDefault() {

        // リポジトリにrequestIdExtractorの定義なし
        SystemRepository.clear();

        assertThat(RequestUtil.getRequestId("/a/b/cde?test=aaa#abc"), is("/a/b/cde"));
        assertThat(RequestUtil.getRequestId(null), is(nullValue()));
    }
}
