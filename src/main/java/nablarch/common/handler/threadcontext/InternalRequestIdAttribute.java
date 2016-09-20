package nablarch.common.handler.threadcontext;

import nablarch.core.ThreadContext;

/**
 * 内部リクエストIDを保持するスレッドコンテキスト属性。
 * 
 * @author Iwauo Tajima
 */
public class InternalRequestIdAttribute extends RequestIdAttribute {
    /** {@inheritDoc}  */
    public String getKey() {
        return ThreadContext.INTERNAL_REQUEST_ID_KEY;
    }
}
