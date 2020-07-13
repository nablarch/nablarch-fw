package nablarch.fw.handler;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;

/**
 * {@link SystemRepository}から委譲対象クラスのインスタンスを取得する
 * デリゲートファクトリ（{@link DelegateFactory}）の実装クラス。
 */
public final class SystemRepositoryDelegateFactory implements DelegateFactory {

    @Override
    public Object create(final Class<?> clazz) {
        final Object delegate = SystemRepository.get(clazz.getName());
        if (delegate == null) {
            throw new IllegalConfigurationException("specified " + clazz.getName() + " is not registered in SystemRepository.");
        }

        return delegate;
    }
}
