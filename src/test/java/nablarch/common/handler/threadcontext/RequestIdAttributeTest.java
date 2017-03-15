package nablarch.common.handler.threadcontext;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import nablarch.core.repository.SystemRepository;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Request;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link RequestIdAttribute}のテストクラス。
 *
 * @author Kiyohito Itoh
 */
public class RequestIdAttributeTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemRepository.clear();
    }

    /**
     * {@link RequestIdAttribute#getValue(Request, ExecutionContext)}のテスト。
     */
    @Test
    public void testResolve() {

        RequestIdAttribute resolver = new RequestIdAttribute();
        assertEquals("aaa/bbb/ccc", resolver.getValue(new MockRequest("aaa/bbb/ccc"), null));
        assertEquals(null, resolver.getValue(new MockRequest(""), null));
        assertEquals("/", resolver.getValue(new MockRequest("/"), null));
        assertEquals(null, resolver.getValue(new MockRequest(null), null));
    }
    
    public static class MockRequest implements Request<String> {

        private final String requestPath;

        public MockRequest() {
            this(null);
        }

        public MockRequest(String requestPath) {
            this.requestPath = requestPath;
        }

        @Override
        public String getRequestPath() {
            return requestPath;
        }

        @Override
        public Request<String> setRequestPath(final String s) {
            return null;
        }

        @Override
        public String getParam(final String s) {
            return null;
        }

        @Override
        public Map<String, String> getParamMap() {
            return null;
        }
    }
}
