package nablarch.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RequestIdExtractor}の実装クラス。<br />
 * リクエストパスの最後の"/"以降を抜き出し、抜き出した文字列から拡張子を取り除いたものをリクエストIDとして扱う。
 * 
 * @author Naoki Yamamoto
 */
public final class ShortRequestIdExtractor implements RequestIdExtractor {
    
    /** リクエストパス中のリクエストIDに相当する部分 */
    private static final Pattern REQUEST_ID_IN_PATH = Pattern.compile("^.*?([^/]+)$");

    /** {@inheritDoc} **/
    @Override
    public String getRequestId(String path) {
        Matcher m = REQUEST_ID_IN_PATH.matcher(path.trim());
        return m.matches() ? m.group(1).replaceAll("\\.[^\\.]*$", "") : null;
    }
}
