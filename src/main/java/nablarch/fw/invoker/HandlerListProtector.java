package nablarch.fw.invoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ハンドラリストを保護するクラス。
 * <p/>
 * デフォルトでは、ハンドラリストのインスタンスは変更に対して保護される。
 * 変更が必要な場合、適切な保護モード{@link ProtectMode}を設定する。
 *
 * @param <T> ハンドラの型
 * @author T.Kawasaki
 */
public class HandlerListProtector<T> {

    /** コンストラクタ。 */
    public HandlerListProtector() {
        this(ProtectMode.UNMODIFIABLE);
    }

    /**
     * コンストラクタ。
     *
     * @param protectMode 保護モード
     */
    public HandlerListProtector(ProtectMode protectMode) {
        this.protectMode = protectMode;
    }

    /**
     * 保護モード。
     * デフォルトは、{@link ProtectMode#UNMODIFIABLE}
     */
    private ProtectMode protectMode;


    public List<T> protect(List<T> original) {
        if (original == null) {
            throw new IllegalArgumentException("argument must not be null.");
        }
        return (List<T>) protectMode.wrap(original);
    }


    /**
     * ハンドラリストの保護モード({@link ProtectMode})を設定する。
     *
     * @param protectModeValue 保護モード
     */
    public void setProtectModeExpression(String protectModeValue) {
        setProtectMode(ProtectMode.valueOf(protectModeValue));
    }

    /**
     * ハンドラリストの保護モード({@link ProtectMode})を設定する。
     *
     * @param protectMode 保護モード
     */
    public void setProtectMode(ProtectMode protectMode) {
        this.protectMode = protectMode;
    }

    /**
     * ハンドラリストの保護モード。
     */
    public enum ProtectMode {

        /** コピーする。 */
        COPY {
            @Override
            <T> List<T> wrap(List<T> handlerList) {
                // ハンドラ追加の可能性があるのでsizeを大きめに設定する。
                List<T> copied = new ArrayList<T>((int) (handlerList.size() * 1.2));
                copied.addAll(handlerList);
                return copied;
            }
        },
        /** 不変にする。 */
        UNMODIFIABLE {
            @Override
            <T> List<T> wrap(List<T> handlerList) {
                return Collections.unmodifiableList(handlerList);
            }
        },
        /** 何もしない。（通常使用しない） */
        NONE {
            @Override
            <T> List<T> wrap(List<T> handlerList) {
                return handlerList;
            }
        };

        /**
         * ハンドラリストを保護する。
         *
         * @param handlerList 保護対象となるハンドラリスト
         * @return 保護されたハンドラリスト
         */
        abstract <T> List<T> wrap(List<T> handlerList);

    }

}
