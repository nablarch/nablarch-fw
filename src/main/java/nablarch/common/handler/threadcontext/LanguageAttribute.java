package nablarch.common.handler.threadcontext;

import java.util.Locale;

import nablarch.core.ThreadContext;
import nablarch.core.util.I18NUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Request;

/**
 * スレッドコンテキストに保持する言語属性。
 * 
 * @author Iwauo Tajima
 */
public class LanguageAttribute implements ThreadContextAttribute<Request<?>> {
    /**
     * {@inheritDoc}
     * <pre>
     * {@link ThreadContext#LANG_KEY} を使用する。
     * </pre>
     */
    public String getKey() {
        return ThreadContext.LANG_KEY;
    }
    
    /**
     * スレッドコンテキストに格納されるデフォルトの言語を設定する。
     * <pre>
     * 明示的に指定しなかった場合、システムデフォルトロケールが使用される。
     * </pre>
     * @param defaultLanguage デフォルトロケールを表す文字列
     * @see Locale#getDefault()
     */
    public void setDefaultLanguage(String defaultLanguage) {
        Locale locale = I18NUtil.createLocale(defaultLanguage);
        this.defaultLanguage = locale;
    }

    /** デフォルトロケール */
    private Locale defaultLanguage = Locale.getDefault();
    
    /**
     * {@inheritDoc}
     * <pre>
     * 現行の実装では初期設定されたデフォルトロケールを返す。
     * </pre>
     */
    public Object getValue(Request<?> req, ExecutionContext ctx) {
        return defaultLanguage;
    }
}
