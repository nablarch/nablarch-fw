package nablarch.fw.results;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Result.ClientError;

/** 入力データの内容に問題がある為に処理が継続できないことを示す例外。 */
@Published(tag = "architect")
public class BadRequest extends ClientError {
    /**
     * デフォルトコンストラクタ
     */
    public BadRequest() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public BadRequest(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public BadRequest(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public BadRequest(String message, Throwable cause) {
        super(message, cause);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "The request could not be processed "
                    + "due to malformed syntax or not being consistent. "
                    + "Please check your request.";

    /** {@inheritDoc} */
    public int getStatusCode() {
        return 400;
    }
}
