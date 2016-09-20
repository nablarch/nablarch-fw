package nablarch.fw.invoker;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;

/**
 * ハンドラリストの起動を行うインタフェース。
 *
 * @author T.Kawasaki
 * @param <TData>    処理対象データ型
 * @param <TResult>  処理結果データ型
 */
@Published(tag = "architect")
public interface HandlerListInvoker<TData, TResult> {

    /**
     * ハンドラリストの起動を行う。
     *
     * @param input 入力データ
     * @param context 実行コンテキスト
     * @return 出力データ
     */
    TResult invokeHandlerList(TData input, ExecutionContext context);

}
