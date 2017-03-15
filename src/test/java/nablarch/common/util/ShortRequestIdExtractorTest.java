package nablarch.common.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * {@link ShortRequestIdExtractor}のテスト。
 *
 * @author Naoki Yamamoto
 */
public class ShortRequestIdExtractorTest {

    /**
     * {@link ShortRequestIdExtractor#getRequestId(String)}のテスト。
     */
    @Test
    public void testGetRequestId() {

        RequestIdExtractor extractor = new ShortRequestIdExtractor();

        assertThat(extractor.getRequestId(""), is(nullValue()));
        assertThat(extractor.getRequestId(" "), is(nullValue()));
        assertThat(extractor.getRequestId("/"), is(nullValue()));
        assertThat(extractor.getRequestId(" /"), is(nullValue()));
        assertThat(extractor.getRequestId(" / "), is(nullValue()));
        assertThat(extractor.getRequestId(" a/ "), is(nullValue()));
        assertThat(extractor.getRequestId(" /a/ "), is(nullValue()));
        assertThat(extractor.getRequestId(" /a/b/ "), is(nullValue()));

        assertThat(extractor.getRequestId(" /a "), is("a"));
        assertThat(extractor.getRequestId(" /a/b/c "), is("c"));
        assertThat(extractor.getRequestId(" /a/b/cde "), is("cde"));

        assertThat(extractor.getRequestId(" /a/b/cde/f.html "), is("f"));
    }
}
