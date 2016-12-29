package nablarch.common.handler.threadcontext;


import nablarch.core.ThreadContext;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.InboundHandleable;
import nablarch.fw.OutboundHandleable;
import nablarch.fw.Result;

/**
 * {@link ThreadContextHandler}で設定した{@link nablarch.core.ThreadContext}上の値をクリアするハンドラ。
 * <p>
 * このハンドラより手前では、復路処理でも{@link ThreadContext}にアクセスすることはできない。
 * このため、極力先頭に設定する必要がある。
 *
 * @author siosio
 */
public class ThreadContextClearHandler implements Handler<Object, Object>, InboundHandleable, OutboundHandleable {

    @Override
    public Object handle(final Object o, final ExecutionContext context) {
        try {
            handleInbound(context);
            return context.handleNext(o);
        } finally {
            handleOutbound(context);
        }
    }

    @Override
    public Result handleInbound(final ExecutionContext context) {
        return new Result.Success();
    }

    @Override
    public Result handleOutbound(final ExecutionContext context) {
        ThreadContext.clear();
        return new Result.Success();
    }
}
