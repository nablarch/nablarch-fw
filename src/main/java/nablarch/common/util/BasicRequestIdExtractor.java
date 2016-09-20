package nablarch.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RequestIdExtractor}の基本実装クラス。<br />
 * リクエストパスから"?"や"#"以降を取り除いた文字列をリクエストIDとして扱う。
 * 
 * @author Naoki Yamamoto
 */
public final class BasicRequestIdExtractor implements RequestIdExtractor {
	
	/** リクエストパス中のリクエストIDに相当する部分 */
    private static final Pattern REQUEST_ID_IN_PATH = Pattern.compile("^([^#\\?]+).*$");
	
    /** {@inheritDoc} **/
    @Override
    public String getRequestId(String path) {
        Matcher m = REQUEST_ID_IN_PATH.matcher(path.trim());
        return m.matches() ? m.group(1) : null; 
    }
}