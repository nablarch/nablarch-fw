package nablarch.fw.handler;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Result;

import java.lang.reflect.Method;

/**
 * ハンドラキューの委譲チェインとは独立したルールに従って、
 * ハンドラのディスパッチを行うハンドラ(ディスパッチャ)
 * 
 * @param <TData>   ハンドラに対する入力オブジェクトの型
 * @param <TResult> ハンドラの処理結果オブジェクトの型
 * @param <TSelf>   具象ハンドラの型
 *
 * @author Iwauo Tajima
 */
public abstract class DispatchHandler<TData, TResult, TSelf extends Handler<TData, TResult>>
implements Handler<TData, TResult> {
    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(DispatchHandler.class);

    /** デリゲートファクトリ */
    private DelegateFactory delegateFactory = new DefaultDelegateFactory();

    /**
     * 処理を委譲するハンドラの型を決定する。
     * 
     * @param  input 入力データ
     * @param  context 実行コンテキスト
     * @return 処理を委譲するハンドラ
     * @throws ClassNotFoundException 指定されたクラスが存在しなかった場合。
     */
    protected abstract Class<?> getHandlerClass(TData input, ExecutionContext context)
    throws ClassNotFoundException;
    
    /**
     * {@inheritDoc}
     * 
     * このクラスの実装では、 #getHandlerClass() で指定されるクラスのインスタンスを生成し、
     * ハンドラキューに追加した後、後続のハンドラに処理を委譲する。
     * 
     * ハンドラの追加位置は{@link #immediate}の値に従って以下のように変化する。
     * <pre>
     *   immediate = true : ハンドラキューの先頭に追加。(即時に実行される。)
     *   immediate = false: ハンドラキューの末尾に追加。 
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public TResult handle(TData req, ExecutionContext ctx) {

        Handler<TData, TResult> handler = null;
        Object delegate = null;
        String fqn = null;
        Class<?> clazz = null;
        try {
            clazz = getHandlerClass(req, ctx);
            fqn = clazz.getName();
            // 委譲先クラスのインスタンスを生成する
            delegate = delegateFactory.create(clazz);
            
            handler = createHandlerFor(delegate, ctx);
            
        } catch (ClassNotFoundException e) {
            // クラス名が存在しない場合は404エラーにする。
            String message = "Couldn't find handler.: " + fqn;
            LOGGER.logInfo(message, e);
            throw new Result.NotFound(message, e);
        
        } catch (InstantiationException e) {
            // Couldn't create an instance because the class was
            // abstract or interface.
            throw new RuntimeException(e);
            
        } catch (IllegalAccessException e) {
            // Couldn't create an instance because access to
            // its constructor was not permitted.
            throw new RuntimeException(e);
        }
        
        if (handler == null) {
            String message = "Couldn't instantiate handler.: " + fqn;
            LOGGER.logInfo(message);
            throw new Result.NotFound(message);
        }
                
        if (immediate) {
            // ハンドラキューの先頭に追加。(直後に実行される。)
            ctx.addHandler(0, handler);
        } else {
            // ハンドラキューの最後尾に追加。
            ctx.addHandler(handler);
        }

        return (TResult) ctx.handleNext(req);
    }
    /**
     * 渡されたインスタンスからハンドラインスタンスを作成して返す。
     * 
     * 指定されたクラスがHandlerインターフェースを実装している場合は
     * そのインスタンスをキャストして返す。 
     * 対象のクラスがハンドラインターフェースを実装していない場合でも、
     * MethodBinderが実行コンテキストに設定されていれば、それを使用して
     * Handlerインターフェースのラッパーを作成して返す。
     * MethodBinderも存在しない場合はnullを返す。
     * @param delegate インスタンス
     * @param ctx 実行コンテキスト
     * @return ハンドラインスタンス        
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Handler<TData, TResult> createHandlerFor(Object delegate, ExecutionContext ctx) {
        if (delegate instanceof Handler) {
            try {
                saveHandlerClassAndMethodToRequestScope(ctx, delegate);
            } catch (NoSuchMethodException e) {
                // 直前に Handler インタフェースを実装していることをチェックしているので、この例外は起こりえない
                throw new RuntimeException(e);
            }
            return (Handler) delegate;
        }
        if (ctx.getMethodBinder() != null) {
            return (Handler) ctx.getMethodBinder().bind(delegate);
        }
        return null;
    }

    private void saveHandlerClassAndMethodToRequestScope(ExecutionContext context, Object delegate) throws NoSuchMethodException {
        Class<?> clazz = delegate.getClass();
        context.setRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS, clazz);

        Method handleMethod = clazz.getMethod("handle", Object.class, ExecutionContext.class);
        context.setRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD, handleMethod);
    }
    
    /** ディスパッチされたハンドラの実行タイミング。 */
    private boolean immediate = true;
    
    /**
     * ディスパッチされたハンドラの実行タイミングを指定する。
     * 
     * @param immediate
     *     trueの場合は、ディスパッチされたハンドラをハンドラキューの先端に追加する。
     *     falseの場合は、ディスパッチされたハンドラをハンドラキューの最後尾に追加する。
     *     
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf setImmediate(boolean immediate) {
        this.immediate = immediate;
        return (TSelf) this;
    }
    
    /**
     * アクセスログにディスパッチ先クラスを出力する。
     * 
     * デフォルトでは何もしない。
     * 必要に応じてオーバーライドすること。
     * 
     * @param data    入力データオブジェクト
     * @param context 実行コンテキスト
     * @param fqn     ディスパッチ先クラスの完全修飾クラス名
     */
    protected void writeDispatchingClassLog(TData data,
                                            ExecutionContext context,
                                            String fqn) {
        //nop
    }

    /**
     * ハンドラファクトリを設定する。
     * 明示的に設定されない場合、デフォルト実装として{@link DefaultDelegateFactory}を使用する。
     * @param delegateFactory ハンドラファクトリ
     */
    public void setDelegateFactory(DelegateFactory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

}