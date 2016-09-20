package nablarch.common.util;

import nablarch.core.repository.SystemRepository;
import nablarch.core.util.StringUtil;


/**
 * リクエスト処理に使用するユーティリティ。
 * @author Kiyohito Itoh
 */
public final class RequestUtil {
    
    /** デフォルトの{@link RequestIdExtractor} */
    private static final RequestIdExtractor DEFAULT_EXTRACTOR = new BasicRequestIdExtractor();

    /** 隠蔽コンストラクタ */
    private RequestUtil() {
    }
    
    /**
     * リクエストパスからリクエストIDに相当する部分を抜き出す。
     * @param path リクエストパス
     * @return リクエストID。見つからない場合はnull
     */
    public static String getRequestId(String path) {
        return StringUtil.hasValue(path) ? getRequestIdExtractor().getRequestId(path) : null;
    }

    /**
     * リポジトリより{@link RequestIdExtractor}の実装クラスを取得する。<br />
     * リポジトリに存在しない場合は、{@link BasicRequestIdExtractor}を取得する。
     * 
     * @return {@link RequestIdExtractor}の実装クラス
     */
    public static RequestIdExtractor getRequestIdExtractor() {
        RequestIdExtractor extractor = SystemRepository.get("requestIdExtractor");
        return extractor == null ? DEFAULT_EXTRACTOR : extractor;
    }
}
