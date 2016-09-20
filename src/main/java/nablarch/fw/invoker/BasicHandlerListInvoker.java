package nablarch.fw.invoker;

import java.util.List;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

/**
 * {@link HandlerListInvoker}の基本実装クラス。
 * 設定された{@link HandlerListBuilder}を使用してハンドラリストを組み立てて、
 * そのハンドラリストを起動する。
 *
 * @param <TData>    処理対象データ型
 * @param <TResult>  処理結果データ型
 *
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public class BasicHandlerListInvoker<TData, TResult> implements HandlerListInvoker<TData, TResult> {

    /** 実行対象となるハンドラリストを組み立てる{@link HandlerListBuilder} */
    private HandlerListBuilder<TData> handlerListBuilder;

    @Override
    public TResult invokeHandlerList(TData input, ExecutionContext context) {
        context.setHandlerQueue(getHandlerList(input));
        return context.handleNext(input);
    }

    /**
     * 実行対象となるハンドラリストを取得する。
     *
     * @param input 入力データ
     * @return ハンドラリスト
     */
    protected List<Handler<?, ?>> getHandlerList(TData input) {
        if (handlerListBuilder == null) {
            throw new IllegalStateException("handlerListBuilder must be set.");
        }
        return handlerListBuilder.getHandlerList(input);
    }

    /**
     * {@link HandlerListBuilder}を設定する。
     *
     * @param handlerListBuilder {@link HandlerListBuilder}
     */
    public void setHandlerListBuilder(HandlerListBuilder<TData> handlerListBuilder) {
        this.handlerListBuilder = handlerListBuilder;
    }
}
