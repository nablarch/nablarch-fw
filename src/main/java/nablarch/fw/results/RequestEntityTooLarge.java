package nablarch.fw.results;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Result.ClientError;

/**
 * 要求されたリクエストが大きすぎるため、処理を継続できないことを示す例外。
 */
@Published(tag = "architect")
public class RequestEntityTooLarge extends ClientError {

    /**
     * デフォルトコンストラクタ
     */
    public RequestEntityTooLarge() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public RequestEntityTooLarge(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public RequestEntityTooLarge(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public RequestEntityTooLarge(String message, Throwable cause) {
        super(message, cause);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "The request you send was refused "
            + "because the request entity is larger than "
            + "the server is willing or able to process.";

    /** {@inheritDoc} */
    @Override
    public int getStatusCode() {
        return 413;
    }
}
