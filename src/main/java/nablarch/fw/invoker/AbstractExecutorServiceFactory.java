package nablarch.fw.invoker;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.initialization.Initializable;
import nablarch.core.util.annotation.Published;

/**
 * {@link ExecutorServiceFactory}の実装をサポートする抽象クラス。
 *
 * {@link ExecutorServiceFactory}の実装クラスで共通となる処理を提供する。
 *
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public abstract class AbstractExecutorServiceFactory implements ExecutorServiceFactory, Initializable {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(AbstractExecutorServiceFactory.class);

    /** デフォルトのタイムアウト（秒）*/
    public static final long DEFAULT_TIMEOUT_SECOND = 10L;

    /** タイムアウト（秒）*/
    protected long timeoutSecond = DEFAULT_TIMEOUT_SECOND;

    /** {@link ExecutorService}*/
    protected ExecutorService executorService;

    @Override
    public void initialize() {
        executorService = createExecutorService();
        if (executorService == null) {
            throw new IllegalStateException("executorService not initialized.");
        }
    }

    /**
     * {@link ExecutorService}インスタンスを生成する。
     * サブクラスにて、インスタンスを生成、必要な設定を行うこと。
     *
     * @return {@link ExecutorService}インスタンス
     */
    protected abstract ExecutorService createExecutorService();

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }


    @Override
    public void startShutdownService() {
        if (!needsShutdown()) {
            return;
        }
        LOGGER.logInfo("starting shutdown. no more request will be accepted.");
        executorService.shutdown();
    }

    @Override
    public void shutdownService() {
        if (executorService == null) {
            // shutdown対象が存在しない場合は何もしない
            return;
        }
        try {
            awaitTermination();
        } catch (InterruptedException e) {
            LOGGER.logInfo("interrupted in ExecutorService#awaitTermination.", e);
            Thread.interrupted();
        }
        executorService.shutdownNow();
        LOGGER.logInfo("shutdown finished.");
    }

    /**
     * スレッド終了を待つ。
     *
     * @return {@link ExecutorService#awaitTermination(long, TimeUnit)}の戻り値
     * @throws InterruptedException 割り込みが発生した場合
     */
    protected boolean awaitTermination() throws InterruptedException {
        return executorService.awaitTermination(timeoutSecond, TimeUnit.SECONDS);
    }

    /**
     * シャットダウン処理を行う必要があるかどうか判定する。
     *
     * @return 必要がある場合、真
     */
    protected boolean needsShutdown() {
        return executorService != null && !executorService.isShutdown();
    }

    /**
     * スレッドの停止を待機する時間（秒）を設定する。
     * @param timeoutSecond スレッドの停止を待機する時間
     */
    public void setTimeoutSecond(long timeoutSecond) {
        this.timeoutSecond = timeoutSecond;
    }
}
