package nablarch.fw.handler;

import nablarch.fw.ExecutionContext;

/**
 * ディスパッチ対象となるクラスのインスタンスを生成するファクトリインタフェース。
 *
 * @author Taichi Uragami
 * @see DispatchHandler#handle(Object, ExecutionContext)
 */
public interface DelegateFactory {

    /**
     * ディスパッチ対象となるクラスのインスタンスを生成する。
     *
     * @param clazz ディスパッチ対象となるクラス
     * @return インスタンス
     * @throws InstantiationException インスタンス生成に失敗した場合
     * @throws IllegalAccessException クラスまたはコンストラクタにアクセスできない場合
     */
    Object create(Class<?> clazz) throws InstantiationException, IllegalAccessException;
}