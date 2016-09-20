package nablarch.fw.results;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.util.annotation.Published;

/**
 * 業務処理が異常終了したことを示す例外クラス。
 *
 * @author hisaaki sioiri
 */
@Published(tag = "architect")
public class TransactionAbnormalEnd extends nablarch.fw.results.InternalError {

    /** 終了コード */
    private final int exitCode;

    /**
     * 終了コードとメッセージ（障害コードとオプション）を元に例外を構築する。
     *
     * @param exitCode 終了コード(プロセスを終了({@link System#exit(int)})する際に設定する値)
     * @param failureCode 障害コード
     * @param messageOptions 障害コードからメッセージを取得する際に使用するオプション情報
     */
    @Published
    public TransactionAbnormalEnd(int exitCode, String failureCode,
            Object... messageOptions) {
        super(LogLevel.FATAL, failureCode, messageOptions);
        validateExitCode(exitCode);
        this.exitCode = exitCode;
    }

    /**
     * 終了コードとメッセージ（障害コードとオプション）、元例外{@link Throwable}を元に例外を構築する。
     * <p/>
     * 元例外が存在しない場合は、{@link #TransactionAbnormalEnd(int, String, Object...)} を使用する。
     *
     * @param exitCode 終了コード(プロセスを終了({@link System#exit(int)})する際に設定する値)
     * @param error 元例外
     * @param failureCode 障害コード
     * @param messageOptions 障害コードからメッセージを取得する際に使用するオプション情報
     */
    @Published
    public TransactionAbnormalEnd(int exitCode,
            Throwable error,
            String failureCode,
            Object... messageOptions) {
        super(LogLevel.FATAL, error, failureCode, messageOptions);
        validateExitCode(exitCode);
        this.exitCode = exitCode;
    }

    /**
     * 設定された終了コードの値のバリデーションを行う。
     *
     * @param exitCode 終了コード
     */
    private static void validateExitCode(int exitCode) {
        if (exitCode < 100 || exitCode > 199) {
            throw new IllegalArgumentException(
                    "Exit code was invalid range. "
                            + "Please set it in the range of 199 from 100. "
                            + "specified value was:" + exitCode
            );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return インスタンス生成時に指定された終了コードを返却する。
     */
    @Published(tag = "architect")
    public int getStatusCode() {
        return exitCode;
    }
}
