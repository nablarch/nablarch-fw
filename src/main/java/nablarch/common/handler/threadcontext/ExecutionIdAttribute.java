package nablarch.common.handler.threadcontext;

import nablarch.core.ThreadContext;
import nablarch.core.log.LogUtil;
import nablarch.fw.ExecutionContext;

/**
 * スレッドコンテキストに保持する実行時ID。
 * 
 * @author Kiyohito Itoh
 */
public class ExecutionIdAttribute implements ThreadContextAttribute<Object> {
    
    /** {@inheritDoc} */
    public String getKey() {
        return ThreadContext.EXECUTION_ID_KEY;
    }
    
    /** {@inheritDoc} */
    public Object getValue(Object req, ExecutionContext ctx) {
        return LogUtil.generateExecutionId();
    }
}
