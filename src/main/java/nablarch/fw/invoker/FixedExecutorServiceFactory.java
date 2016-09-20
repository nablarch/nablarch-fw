package nablarch.fw.invoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 固定数のスレッドプールを使用する{@link ExecutorService}を生成するクラス。
 *
 * デフォルトでは、利用可能なCPU数({@link Runtime#availableProcessors()})を2倍した数のスレッドを使用する。
 *
 * @author T.Kawasaki
 * @see Executors#newFixedThreadPool(int)
 */
public class FixedExecutorServiceFactory extends AbstractExecutorServiceFactory {

    /** デフォルトのプールサイズ */
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    /** スレッドプールのサイズ */
    private int threadPoolSize = DEFAULT_POOL_SIZE;

    @Override
    protected ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * スレッドプールのサイズを設定する。
     *
     * @param threadPoolSize スレッドプールのサイズ
     */
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}
