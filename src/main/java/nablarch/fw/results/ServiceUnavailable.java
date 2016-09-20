package nablarch.fw.results;

import java.util.Date;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.util.annotation.Published;

/** 一時的に処理の受付を停止していることを表す例外。 */
@Published(tag = "architect")
public class ServiceUnavailable extends ServiceError {
    /**
     * デフォルトコンストラクタ
     */
    public ServiceUnavailable() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public ServiceUnavailable(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public ServiceUnavailable(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public ServiceUnavailable(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * コンストラクタ
     * @param logLevel      運用ログの出力レベル
     * @param messageId     エラーメッセージのID
     * @param messageParams エラーメッセージの埋め込みパラメータ
     */
    public ServiceUnavailable(LogLevel logLevel,
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
    public ServiceUnavailable(LogLevel logLevel,
                              Throwable cause,
                              String messageId,
                              Object... messageParams) {
        super(logLevel, cause, messageId, messageParams);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "This service is currently unable to handle your request "
                    + "due to a temporary overloading or maintenance. ";

    /** {@inheritDoc} */
    public int getStatusCode() {
        return 503;
    }

    /**
     * 処理受付が再開される予定時刻を設定する。
     *
     * @param retryAfter 再開予定時刻
     * @return 自身のインスタンス
     */
    public ServiceUnavailable setRetryAfter(Date retryAfter) {
        this.retryAfter = (retryAfter == null)
                ? null
                : retryAfter.getTime();
        return this;
    }

    /** 処理受付が再開される予定時刻(Unix-Time) */
    private Long retryAfter = null;

    /**
     * 処理受付が再開される予定時刻を返す。
     * デフォルトはnull。(=再開時期未定)
     *
     * @return 再開予定時間
     */
    public Date getRetryAfter() {
        return (retryAfter == null) ? null
                : new Date(retryAfter);
    }
}
