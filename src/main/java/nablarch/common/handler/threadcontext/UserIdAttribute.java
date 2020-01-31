package nablarch.common.handler.threadcontext;

import nablarch.core.ThreadContext;
import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;

/**
 * スレッドコンテキストに保持するユーザID属性。
 * <pre>
 * HTTPセッション上に格納されているログインユーザIDを
 * スレッドコンテキストに格納する。
 * </pre>
 *
 * @author Iwauo Tajima <iwauo@tis.co.jp>
 */
public class UserIdAttribute implements ThreadContextAttribute<Object> {
    /**
     * ログインユーザIDが格納されているHTTPセッション上のキー名を設定する。
     * <pre>
     * デフォルトでは{@link #getKey()}の値を使用する。
     * </pre>
     *
     * @param sessionKey HTTPセッション上のキー名
     */
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    /** ログインユーザIDが格納されているHTTPセッション上のキー名 */
    private String sessionKey = null;

    /**
     * 未ログイン時にスレッドコンテキストに設定されるIDを設定する。
     * <pre>
     * 明示的にこの値を設定しなかった場合、
     * 未ログイン時にスレッドコンテキスト上のユーザIDは設定されない。
     * </pre>
     *
     * @param anonymousId 未ログイン時にスレッドコンテキストに設定されるID
     */
    public void setAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    /** 未ログイン時にスレッドコンテキストに設定されるID */
    private String anonymousId = null;

    /**
     * {@inheritDoc}
     * <pre>
     * {@link ThreadContext#USER_ID_KEY} を使用する。
     * </pre>
     */
    public String getKey() {
        return ThreadContext.USER_ID_KEY;
    }

    /**
     * {@inheritDoc}
     * <pre>
     * スレッドコンテキストに格納するユーザIDの値は以下のように決定される。
     *   1. HTTPセッション上のキー{@link #sessionKey} の値を取得する。
     *      その値がnullでなければスレッドコンテキストに設定する。
     *   2. HTTPセッション上の値がnullであり、かつ {@link #anonymousId} が
     *      設定されていれば、その値をスレッドコンテキストに設定する。
     *   3. 上記以外の場合はnullを設定する。
     * </pre>
     */
    public Object getValue(Object req, ExecutionContext ctx) {
        String skey = StringUtil.isNullOrEmpty(sessionKey)
                ? this.getKey()
                : sessionKey;
        String userId = (String) getUserIdSession(ctx, skey);
        if (anonymousId != null && userId == null) {
            userId = anonymousId;
        }
        return userId;
    }

    protected Object getUserIdSession(ExecutionContext ctx, String skey) {
        return ctx.getSessionScopedVar(skey);
    }
}
