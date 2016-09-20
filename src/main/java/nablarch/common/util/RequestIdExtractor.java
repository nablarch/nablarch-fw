package nablarch.common.util;

import nablarch.core.util.annotation.Published;

/**
 * リクエストIDの抽出を行うインターフェース。
 *
 * @author Naoki Yamamoto
 */
@Published(tag = "architect")
public interface RequestIdExtractor {
    
    /**
     * リクエストパスからリクエストIDに相当する部分を抜き出す。
     * @param path リクエストパス
     * @return リクエストID
     */
    String getRequestId(String path);
    
}
