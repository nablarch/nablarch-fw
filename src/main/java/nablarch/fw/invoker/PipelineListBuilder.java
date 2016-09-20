package nablarch.fw.invoker;

import java.util.List;

import nablarch.core.util.annotation.Published;

/**
 * ハンドラリストの組み立てを行うインタフェース。
 *
 * @author Koichi Asano
 */
@Published(tag = "architect")
public interface PipelineListBuilder {

    /**
     * ハンドラリストを取得する。
     * @return ハンドラリスト
     */
    List<Object> getHandlerList();

}
