package nablarch.common.handler.threadcontext;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;

/**
 * {@link nablarch.core.ThreadContext}に設定する属性を返すインタフェース。
 * <p/>
 * 本インタフェースを実装したクラスは、スレッドコンテキストに設定する値を取得する責務を持つ。
 *
 * @param <T> ハンドラの入力データの型
 * @see nablarch.core.ThreadContext
 *
 * @author Iwauo Tajima
 */
@Published
public interface ThreadContextAttribute<T> {
    /**
     * スレッドコンテキストに格納する際に使用するプロパティのキー名を返す。
     * @return プロパティのキー名
     */
    String getKey();
    
    /**
     * スレッドコンテキストに格納するプロパティの値を返す。
     * @param req ハンドラの入力データ
     * @param ctx 実行コンテキスト情報
     * @return プロパティの値
     */
    Object getValue(T req, ExecutionContext ctx);
}
