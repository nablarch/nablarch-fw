package nablarch.fw.invoker;

import java.util.concurrent.ExecutorService;

import nablarch.core.util.annotation.Published;

/**
 * {@link ExecutorService}を生成するためのファクトリインタフェース。
 *
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public interface ExecutorServiceFactory {

    /**
     * {@link ExecutorService}を取得する。
     *
     * @return {@link ExecutorService}
     */
    ExecutorService getExecutorService();

    /**
     * シャットダウンを開始する。
     * このメソッド起動後は、以降の要求は受け付けられない。
     */
    void startShutdownService();

    /**
     * シャットダウンを行う。
     */
    void shutdownService();
}
