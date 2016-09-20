package nablarch.fw.results;

import nablarch.core.util.annotation.Published;
import nablarch.fw.Result.ClientError;

/**
 * 要求された処理が既に行われた(もしくは並行して実行されている)
 * 処理の結果と競合しているため、処理を継続することができないことを示す例外。
 */
@Published(tag = "architect")
public class Conflicted extends ClientError {
    /**
     * デフォルトコンストラクタ
     */
    public Conflicted() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public Conflicted(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param cause 起因となる例外
     */
    public Conflicted(Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause   起因となる例外
     */
    public Conflicted(String message, Throwable cause) {
        super(message, cause);
    }

    /** デフォルトメッセージ */
    private static final String
    DEFAULT_MESSAGE = "The request could not be processed "
                    + "due to a conflict with the current status of "
                    + "the resource you requested or a concurrent request "
                    + "accessing the same resource.";

    /** {@inheritDoc} */
    public int getStatusCode() {
        return 409;
    }
}
