package nablarch.fw.invoker;


import java.util.List;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Handler;

/**
 * 事前に設定されたハンドラリストを返却する{@link HandlerListBuilder}実装クラス。
 *
 * @author T.Kawasaki
 * @param <TData>    処理対象データ型
 */
@Published(tag = "architect")
public class BasicHandlerListBuilder<TData> implements HandlerListBuilder<TData> {

    private HandlerListProtector<Handler<?, ?>> protector = new HandlerListProtector<Handler<?, ?>>();

    /** ハンドラリスト */
    private List<Handler<?, ?>> handlerList;

    @Override
    public List<Handler<?, ?>> getHandlerList(TData unused) {
        if (handlerList == null) {
            throw new IllegalStateException("handlerList must be set.");
        }
        return protector.protect(handlerList);
    }

    /**
     * ハンドラリストを設定する。
     *
     * @param handlerList ハンドラリスト
     */
    public void setHandlerList(List<Handler<?, ?>> handlerList) {
        this.handlerList = handlerList;
    }

    /**
     * 保護モードを設定する。
     * @param protectMode 保護モード
     * @see HandlerListProtector.ProtectMode
     */
    public void setProtectMode(String protectMode) {
        protector.setProtectModeExpression(protectMode);
    }
}
