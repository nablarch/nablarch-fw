package nablarch.fw.invoker;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;

/**
 * 非同期でハンドラリストの起動を行う{@link HandlerListInvoker}の実装クラス。
 *
 * @author T.Kawasaki
 * @param <TData>    処理対象データ型
 * @param <TResult>  処理結果データ型
 */
public class AsyncHandlerListInvoker<TData, TResult> implements HandlerListInvoker<TData, Future<TResult>> {

    /** {@link HandlerListInvoker}インスタンス */
    private HandlerListInvoker<TData, TResult> handlerListInvoker;

    /** Nablarchのハンドラリストを実行するためのスレッドを生成する{@link ExecutorServiceFactory} */
    private ExecutorServiceFactory executorServiceFactory;

    /**
     * {@code AsyncHandlerListInvoker}を生成する。
     */
    @Published(tag = "architect")
    public AsyncHandlerListInvoker() {
    }

    @Override
    public Future<TResult> invokeHandlerList(TData input, ExecutionContext context) {
        Callable<TResult> caller = createCallable(getHandlerListInvoker(), input, context);
        ExecutorService service = getExecutorService();
        return service.submit(caller);
    }

    /**
     * ハンドラリストを起動する{@link Callable}インスタンスを生成する。
     * ハンドラリストの起動方法を変更したい場合や、起動前後に処理を追加したい場合、
     * 本メソッドをオーバーライドしてよい。
     *
     * @param handlerListInvoker {@link HandlerListInvoker}
     * @param input 入力データ
     * @param context {@link ExecutionContext}
     * @return {@link Callable}インスタンス
     */
    @Published(tag = "architect")
    protected Callable<TResult> createCallable(
            final HandlerListInvoker<TData, TResult> handlerListInvoker,
            final TData input,
            final ExecutionContext context) {

        return new Callable<TResult>() {
            @Override
            public TResult call() throws Exception {
                return handlerListInvoker.invokeHandlerList(input, context);
            }
        };
    }

    /**
     * {@link HandlerListInvoker}を取得する。
     *
     * @return {@link HandlerListInvoker}インスタンス
     */
    private HandlerListInvoker<TData, TResult> getHandlerListInvoker() {
        if (handlerListInvoker == null) {
            throw new IllegalStateException("handlerListInvoker must be set.");
        }
        return handlerListInvoker;
    }

    /**
     * {@link ExecutorServiceFactory}インスタンスを取得する。
     *
     * @return {@link ExecutorServiceFactory}インスタンス
     */
    private ExecutorService getExecutorService() {
        if (executorServiceFactory == null) {
            throw new IllegalStateException("executorServiceFactory must be set.");
        }
        return executorServiceFactory.getExecutorService();
    }

    /**
     * {@link HandlerListInvoker}を設定する。
     * ここで設定された{@link HandlerListInvoker}を使用して、
     * ハンドラリストを起動する。
     *
     * @param handlerListInvoker {@link HandlerListInvoker}インスタンス
     */
    public void setHandlerListInvoker(
            HandlerListInvoker<TData, TResult> handlerListInvoker) {
        this.handlerListInvoker = handlerListInvoker;
    }

    /**
     * {@link ExecutorServiceFactory}を設定する。
     * ここで設定されたファクトリから、{@link ExecutorService}を取得して、
     * ハンドラリストを起動する。
     *
     * @param executorServiceFactory {@link ExecutorServiceFactory}
     */
    public void setExecutorServiceFactory(ExecutorServiceFactory executorServiceFactory) {
        this.executorServiceFactory = executorServiceFactory;
    }

}
