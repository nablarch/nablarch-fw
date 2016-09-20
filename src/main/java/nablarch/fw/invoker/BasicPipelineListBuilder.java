package nablarch.fw.invoker;


import java.util.List;

import nablarch.core.util.annotation.Published;

/**
 * 事前に設定されたハンドラリストを返却する{@link PipelineListBuilder}実装クラス。
 *
 * @author Koichi Asano
 */
@Published(tag = "architect")
public class BasicPipelineListBuilder implements PipelineListBuilder {

    /** ハンドラリストを保護するオブジェクト */
    private final HandlerListProtector<Object> protector = new HandlerListProtector<Object>();

    /** ハンドラリスト */
    private List<Object> handlerList;

    @Override
    public List<Object> getHandlerList() {
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
    public void setHandlerList(List<Object> handlerList) {
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
