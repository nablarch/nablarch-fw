package nablarch.common.handler.threadcontext;

import java.util.TimeZone;

import nablarch.core.ThreadContext;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Request;

/**
 * スレッドコンテキストに保持するタイムゾーン属性。
 * 
 * @author Kiyohito Itoh
 */
public class TimeZoneAttribute implements ThreadContextAttribute<Request<?>> {

    /**
     * {@inheritDoc}
     * <pre>
     * {@link ThreadContext#TIME_ZONE_KEY} を使用する。
     * </pre>
     */
    public String getKey() {
        return ThreadContext.TIME_ZONE_KEY;
    }

    /**
     * スレッドコンテキストに格納されるデフォルトのタイムゾーンを設定する。
     * <pre>
     * 明示的に指定しなかった場合、システムのデフォルトタイムゾーンが使用される。
     * </pre>
     * @param defaultTimeZone デフォルトタイムゾーンを表す文字列
     * @see TimeZone#getDefault()
     */
    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = TimeZone.getTimeZone(defaultTimeZone);
    }

    /** デフォルトタイムゾーン */
    private TimeZone defaultTimeZone = TimeZone.getDefault();

    /**
     * {@inheritDoc}
     * <pre>
     * 現行の実装では初期設定されたデフォルトタイムゾーンを返す。
     * </pre>
     */
    public Object getValue(Request<?> req, ExecutionContext ctx) {
        return defaultTimeZone;
    }
}
