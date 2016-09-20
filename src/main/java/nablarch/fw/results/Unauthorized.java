package nablarch.fw.results;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Result.ClientError;

/**
 * 必要な認証プロセスを経ていないため、処理を継続することができない
 * ことを示す例外。
 */
@Published(tag = "architect")
public class Unauthorized extends ClientError {
    /**
     * デフォルトコンストラクタ
     */
    public Unauthorized() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public Unauthorized(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public Unauthorized(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public Unauthorized(String message, Throwable cause) {
        super(message, cause);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "The request you send requires authentication.";

    /** {@inheritDoc} */
    public int getStatusCode() {
        return 401;
    }
}
