package nablarch.fw.results;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Result.ClientError;

/**
 * 必要な権限が無いため、処理を継続することができない
 * ことを示す例外。
 */
@Published(tag = "architect")
public class Forbidden extends ClientError {
    /**
     * デフォルトコンストラクタ
     */
    public Forbidden() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public Forbidden(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public Forbidden(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public Forbidden(String message, Throwable cause) {
        super(message, cause);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "The request you send was refused "
                    + "due to insufficient authorization.";

    /** {@inheritDoc} */
    public int getStatusCode() {
        return 403;
    }
}
