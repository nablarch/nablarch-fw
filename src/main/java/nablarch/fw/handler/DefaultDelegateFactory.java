package nablarch.fw.handler;

/**
 * デリゲートファクトリ（{@link DelegateFactory}）のデフォルト実装クラス。
 * 本実装では、与えられた委譲対象クラスのインスタンスを{@link Class#newInstance()}により生成する。
 * {@link DispatchHandler#setDelegateFactory(DelegateFactory)}に本クラスを設定することで、
 * 5u14までの{@link DispatchHandler}と全く同じ動作となる。
 *
 * @author Taichi Uragami
 */
public class DefaultDelegateFactory implements DelegateFactory {

    @Override
    public Object create(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }
}