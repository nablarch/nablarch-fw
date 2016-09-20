package nablarch.fw.results;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.util.annotation.Published;

/**
 * ハンドラの内部処理で発生した問題により、処理が継続できないことを
 * 示す例外。
 */
@Published(tag = "architect")
public class InternalError extends ServiceError {

    /** 処理継続が不可能であることを示すステータスコード */
    public static final int STATUS_CODE = 500;

    /**
     * デフォルトコンストラクタ
     */
    public InternalError() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public InternalError(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public InternalError(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public InternalError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * コンストラクタ
     * @param logLevel      運用ログの出力レベル
     * @param messageId     エラーメッセージのID
     * @param messageParams エラーメッセージの埋め込みパラメータ
     */
    public InternalError(LogLevel logLevel,
                         String messageId,
                         Object... messageParams) {
        super(logLevel, messageId, messageParams);
    }

    /**
     * コンストラクタ
     * @param logLevel      運用ログの出力レベル
     * @param cause         起因となる例外
     * @param messageId     エラーメッセージのID
     * @param messageParams エラーメッセージの埋め込みパラメータ
     */
    public InternalError(LogLevel logLevel,
                         Throwable cause,
                         String messageId,
                         Object... messageParams) {
        super(logLevel, cause, messageId, messageParams);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "The request could not be processed "
                    + "due to a unexpected condition. "
                    + "please contact our support team if you need.";

    /** {@inheritDoc} */
    public int getStatusCode() {
        return STATUS_CODE;
    }
}
