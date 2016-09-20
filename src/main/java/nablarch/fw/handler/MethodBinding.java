package nablarch.fw.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.HandlerWrapper;
import nablarch.fw.Interceptor;
import nablarch.fw.Result;
import nablarch.fw.Result.NotFound;

/**
 * メソッドレベルのディスパッチ機能を実装する際に継承する抽象基底クラス。
 * 
 * @param <TData>   入力データの型
 * @param <TResult> 結果データの型
 * 
 * @author Iwauo Tajima
 */
public abstract class MethodBinding<TData, TResult>
implements HandlerWrapper<TData, TResult> {
    // ------------------------------------------------ structure
    /** ディスパッチの対象となるオブジェクト */
    private final Object delegate;
    
    // ------------------------------------------------ constructor
    /**
     * コンストラクタ。
     * @param delegate 委譲対象となるオブジェクト
     */
    public MethodBinding(Object delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }
    
    // ------------------------------ the api must be implemented by subclasses 
    /**
     * 入力データおよび実行コンテキストの内容に応じて、委譲対象のメソッドを決定する。
     * @param data 入力データ
     * @param ctx  実行コンテキスト
     * @return 委譲対象メソッド
     */
    protected abstract Method getMethodBoundTo(TData data, ExecutionContext ctx);
    

    // ------------------------------------------------------------- helper
    /**
     * 委譲対象オブジェクトのメソッドの中から、与えられたメソッド名をもち、
     * かつ、Handler.handle() メソッドと互換なシグニチャを持てばtrueを返す。
     * すなわち、以下の条件を満たすメソッドを返す。
     * 
     * <pre>
     *   1. 引数で渡された文字列と同じメソッド名をもつ。(大文字小文字は同一視)
     *   2. メソッドの修飾子がパブリックかつ非スタティックである。
     *   3. 引数を2つもち、第2引数の型がExecutionContextである。
     * </pre>
     *
     * なお、該当するメソッドが存在しなかった場合はnullを返す。
     *
     * @param name 委譲対象となるメソッド名
     * @return 委譲対象となるメソッド
     *          (該当するメソッドが存在しなかった場合はnull)
     */
    protected Method getHandleMethod(String name) {
        name = name.toLowerCase();
        for (Method method : delegate.getClass().getMethods()) {
            if (!method.getName().toLowerCase().equals(name)) {
                continue;
            }
            if (qualifiesAsHandler(method)) {
                method.setAccessible(true); // 無名クラスへのアクセスを許可
                return method;
            }
        }
        return null;
    }
    
    // ------------------------------------------------ Handler I/F
    /**
     * getMethodBoundTo() で取得したメソッドに対して後続処理を委譲し、
     * その結果を返す。
     * @param req 入力オブジェクト
     * @param ctx 実行コンテキスト
     * @return 処理結果オブジェクト
     * @throws NotFound 入力データに対応するメソッドが存在しない場合。
     */
    public TResult handle(TData req, ExecutionContext ctx) 
    throws NotFound {
        final Method boundMethod = getMethodBoundTo(req, ctx); 
        if (boundMethod == null) {
            throw new Result.NotFound(
                "Couldn't find method to delegate.: " + req.toString()
            );
        }
        Handler<TData, TResult> handler = new Handler<TData, TResult>() {
            @SuppressWarnings("unchecked")
            public TResult handle(TData req, ExecutionContext ctx) {
                try {
                    return (TResult) boundMethod.invoke(delegate, req, ctx);

                } catch (IllegalAccessException e) {
                    // 事前にチェックしているのでここにはこないはず。
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    // 委譲先のメソッドで例外が送出された場合。
                    Throwable cause = e.getCause();
                    if (RuntimeException.class.isAssignableFrom(cause.getClass())) {
                        throw (RuntimeException) cause;
                    }
                    if (Error.class.isAssignableFrom(cause.getClass())) {
                        throw (Error) cause;
                    }
                    throw new RuntimeException(cause);
                }
            }
        };
        return Interceptor.Factory.wrap(handler, boundMethod.getAnnotations())
                                  .handle(req, ctx);
    }
    
    /** {@inheritDoc} */
    public List<Object> getDelegates(TData data, ExecutionContext context) {
        return Arrays.asList(delegate);
    }
    
    /**
     * 与えられたメソッドがhandle()メソッドと互換なシグニチャを持てばtrueを返す。
     *
     * @param method 検証対象のメソッド
     * @return 与えられたメソッドのシグニチャがhandle()と互換ならtrue。
     */
    protected boolean qualifiesAsHandler(Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
            return false;
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        return paramTypes.length == 2
            && paramTypes[1].equals(ExecutionContext.class);
    }
}
