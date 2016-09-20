package nablarch.fw.results;

import java.util.Locale;

import nablarch.core.ThreadContext;
import nablarch.core.log.app.FailureLogUtil;
import nablarch.core.log.basic.LogLevel;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;

/**
 * サービス側で生じた問題により処理が継続できないことを示す例外。
 * <p/>
 * 問題解決には、サービス側での対処が必要となるため、エラーメッセージの内容として、
 * 呼び出し側が問題が発生したことをサービス管理者に連絡する方法と、
 * 管理者に伝えるべき内容を含める必要がある。
 *
 * また、メッセージIDを設定することにより、
 * 運用ログへの出力に関する制御を行うことができる。
 */
@Published(tag = "architect")
public abstract class ServiceError extends nablarch.fw.Result.Error {

    /**
     * デフォルトコンストラクタ
     */
    public ServiceError() {
        super();
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public ServiceError(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public ServiceError(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public ServiceError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 運用ログへの出力に関する制御情報を含む例外を生成する。
     *
     * @param logLevel ログ出力レベル
     * @param messageId ログ内容のメッセージID
     * @param messageParams ログメッセージの埋め込みパラメータ
     */
    public ServiceError(LogLevel logLevel,
                        String messageId,
                        Object... messageParams) {
        super();
        assert (logLevel != null && messageId != null);
        this.logLevel  = logLevel;
        this.messageId = messageId;
        this.messageParams = messageParams;

    }

    /**
     * 運用ログへの出力に関する制御情報を含む例外を生成する。
     *
     * @param logLevel ログ出力レベル
     * @param cause 障害の起因となる例外
     * @param messageId ログ内容のメッセージID
     * @param messageParams ログメッセージの埋め込みパラメータ
     */
    public ServiceError(LogLevel logLevel,
                        Throwable cause,
                        String messageId,
                        Object... messageParams) {
        super(cause);
        assert (logLevel != null && messageId != null);
        this.logLevel  = logLevel;
        this.messageId = messageId;
        this.messageParams = messageParams;
    }

    // ---------------------------------------------- syslog
    /** ログレベル */
    private LogLevel logLevel = LogLevel.FATAL;

    /** メッセージID */
    private String messageId = null;

    /** メッセージ埋め込みパラメータ */
    private Object[] messageParams = new Object[]{};

    /** ログのオプション情報 */
    private Object[] logOptions = new Object[]{};

    /**
     * メッセージIDを返す。
     * @return メッセージID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * メッセージパラメータを返す。
     * @return メッセージパラメータ
     */
    public Object[] getMessageParams() {
        return messageParams;
    }

    /**{@inheritDoc}
     * <p/>
     * このインスタンスにメッセージIDが指定されている場合は、
     * そのIDに対応したメッセージ内容を返す。
     */
    @Override
    public String getMessage() {
        if (messageId == null) {
            return super.getMessage();
        }
        MessageLevel level = null;
        switch (logLevel) {
        case FATAL:
        case ERROR:
            level = MessageLevel.ERROR;
            break;
        case WARN:
            level = MessageLevel.WARN;
            break;
        default:
            level = MessageLevel.INFO;
        }

        try {
            Message message = MessageUtil.createMessage(
                    level, messageId, messageParams
            );
            return message.formatMessage(
                    ThreadContext.getLanguage() != null ? ThreadContext.getLanguage() : Locale.getDefault());

        } catch (Throwable e) {
            return "An error happened with messageId = [" + messageId + "]"
                 + "(but couldn't get the message contents.)";
        }
    }

    /**
     * この障害の内容について運用ログに出力する。
     * <p/>
     * ログレベルがエラーレベル以上の場合に、障害内容を運用ログに出力する。
     * ワーニングレベル以下の場合は何もしない。
     *
     * @param context 実行コンテキスト
     */
    public void writeLog(ExecutionContext context) {
        Object data = context != null ? context.getDataProcessedWhenThrown(this) : null;
        switch (logLevel) {
        case FATAL:
            FailureLogUtil.logFatal(
                    this, data, messageId, messageParams, logOptions
            );
            break;
        case ERROR:
            FailureLogUtil.logError(
                this, data, messageId, messageParams, logOptions
            );
            break;
        default:
            // nop
        }
    }

    /** {@inheritDoc} */
    public int getStatusCode() {
        return 500;
    }

}
