package nablarch.common.handler.threadcontext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nablarch.core.ThreadContext;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.InboundHandleable;
import nablarch.fw.OutboundHandleable;
import nablarch.fw.Result;

/**
 * スレッドコンテキストに保持される共通属性を管理するハンドラ。
 * 
 * フレームワークには、スレッドコンテキストにユーザID・リクエストID・言語設定を保持する実装が含まれている。
 * これらを有効化するには以下のリポジトリ設定を追加する。
 *   (同様にプロジェクト固有の属性を追加することも可能である。)
 * <pre>
 * &lt;component class="nablarch.common.handler.threadcontext.ThreadContextHandler"&gt;
 *   &lt;property name="attributes"&gt;
 *     &lt;list&gt;
 *       &lt;!-- ユーザID --&gt;
 *       &lt;component class="nablarch.common.handler.threadcontext.UserIdAttribute"&gt;
 *         &lt;property name="sessionKey"  value="user.id" /&gt;
 *         &lt;property name="anonymousId" value="guest" /&gt;
 *       &lt;/component&gt;
 *       
 *       &lt;!-- リクエストID --&gt;
 *       &lt;component class="nablarch.common.handler.threadcontext.RequestIdAttribute" /&gt;
 *       
 *       &lt;!-- 言語 --&gt;
 *       &lt;component class="nablarch.common.handler.threadcontext.LanguageAttribute"&gt;
 *           &lt;property name="defaultLanguage" value="ja" /&gt;
 *       &lt;/component&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/component&gt;
 * </pre>
 */
public class ThreadContextHandler implements Handler<Object, Object>, InboundHandleable, OutboundHandleable {

    /**
     * 引数に渡されたスレッドコンテキスト属性を管理するハンドラを生成する。
     * <pre>
     * このメソッドの処理は以下のソースコードと等価である。
     * 
     *     new ThreadContextHandler()
     *         .setAttributes(Arrays.asList(attributes))
     * </pre>
     * @param attributes スレッドコンテキスト属性
     */
    @SuppressWarnings("rawtypes")
    public ThreadContextHandler(ThreadContextAttribute... attributes) {
        this.setAttributes(Arrays.asList(attributes));
    }
    
    /**
     * デフォルトコンストラクタ
     */
    public ThreadContextHandler() {
    }
    
    /**
     * {@inheritDoc}
     * <pre>
     * このクラスの実装では以下の処理を行う。
     * 
     *   1. スレッドコンテキスト上の全てのエントリを削除する。
     *   2. このハンドラに登録されている全ての属性について、
     *      キー(ThreadContextAttribute#getKey()の結果)と値(ThreadContextAttribute#getValue()の結果)を
     *      スレッドコンテキストに格納する。
     *   3. 後続のリクエストハンドラに処理を委譲する。
     * </pre>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object handle(Object input, ExecutionContext ctx) {
        handleInbound(ctx);
        return ctx.handleNext(input);
    }
    
    /**
     * このハンドラが管理する属性のリストを登録する。
     * @param attributes このハンドラが管理する属性のリスト
     * @return このオブジェクト自体
     */
    @SuppressWarnings("rawtypes")
    public ThreadContextHandler setAttributes(List<ThreadContextAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    /** このハンドラが管理する属性のリスト */
    @SuppressWarnings("rawtypes")
    private List<ThreadContextAttribute>
        attributes = new ArrayList<ThreadContextAttribute>();
    @Override
    public Result handleInbound(ExecutionContext context) {
        assert attributes != null;
        
        // スレッドコンテキストに値を設定するまえに、クリア
        ThreadContext.clear();

        for (ThreadContextAttribute attribute : attributes) {
            ThreadContext.setObject(
                attribute.getKey(),
                attribute.getValue(context.getCurrentRequestObject(), context)
            );
        }
        return new Result.Success();
    }

    @Override
    public Result handleOutbound(ExecutionContext context) {
        return new Result.Success();
    }
}
