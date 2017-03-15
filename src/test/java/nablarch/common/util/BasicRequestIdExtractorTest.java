package nablarch.common.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * {@link BasicRequestIdExtractor}のテスト。
 *
 * @author Naoki Yamamoto
 */
public class BasicRequestIdExtractorTest {

    /**
     * {@link BasicRequestIdExtractor#getRequestId(String)}のテスト。
     */
    @Test
    public void testGetRequestId() {

        RequestIdExtractor extractor = new BasicRequestIdExtractor();
        assertThat(extractor.getRequestId(""), is(nullValue()));
        assertThat(extractor.getRequestId(" "), is(nullValue()));
        assertThat(extractor.getRequestId("/"), is("/"));
        assertThat(extractor.getRequestId("/a/b/cde"), is("/a/b/cde"));
        assertThat(extractor.getRequestId(" /a/b/cde "), is("/a/b/cde"));
        assertThat(extractor.getRequestId("/a/b/cde#aaa"), is("/a/b/cde"));
        assertThat(extractor.getRequestId("/a/b/cde#"), is("/a/b/cde"));
        assertThat(extractor.getRequestId("/a/b/cde?test=aaa"), is("/a/b/cde"));
        assertThat(extractor.getRequestId("/a/b/cde?"), is("/a/b/cde"));
        assertThat(extractor.getRequestId("/a/b/cde="), is("/a/b/cde="));
        assertThat(extractor.getRequestId("/a/b/cde?test=aaa#abc"), is("/a/b/cde"));
        assertThat(extractor.getRequestId("/a/b/cde.do"), is("/a/b/cde.do"));
    }
}
