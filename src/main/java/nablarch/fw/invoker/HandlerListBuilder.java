package nablarch.fw.invoker;

import java.util.List;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Handler;

/**
 * ハンドラリストの組み立てを行うインタフェース。
 *
 * @param <TData> 処理対象データ型
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public interface HandlerListBuilder<TData> {

    /**
     * ハンドラリストを取得する。
     * @param input 処理対象データ
     * @return ハンドラリスト
     */
    List<Handler<?, ?>> getHandlerList(TData input);
}
