package nablarch.common.handler.threadcontext;

import nablarch.common.util.RequestUtil;
import nablarch.core.ThreadContext;
import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Request;

/**
 * スレッドコンテキストに保持するリクエストID属性。
 * 
 * @author Kiyohito Itoh
 */
public class RequestIdAttribute implements ThreadContextAttribute<Request<?>> {
    /**
     * {@inheritDoc}
     * <pre>
     * {@link nablarch.core.ThreadContext#REQUEST_ID_KEY} を使用する。
     * </pre>
     */
    public String getKey() {
        return ThreadContext.REQUEST_ID_KEY;
    }

    /**
     * {@inheritDoc}
     * <pre>
     * このクラスではHTTPリクエストURI中からリクエストIDに相当する部分を抜き出して返却する。
     * </pre>
     */
    public Object getValue(Request<?> req, ExecutionContext ctx) {
        assert req != null;
        if (StringUtil.isNullOrEmpty(req.getRequestPath())) {
            return null;
        }
        return RequestUtil.getRequestId(req.getRequestPath());
    }
}
