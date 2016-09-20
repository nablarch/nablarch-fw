package nablarch.fw.invoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExceptionHandler;
import nablarch.fw.ExecutionContext;
import nablarch.fw.InboundHandleable;
import nablarch.fw.OutboundHandleable;
import nablarch.fw.Result;
import nablarch.fw.handler.GlobalErrorHandler;

/**
 * 事前処理、事後処理を行うInvoker.
 * 
 * @author Koichi Asano
 */
@Published(tag = "architect")
public class PipelineInvoker {
    
    /**
     * 処理済ハンドラリストのキー。
     */
    public static final String PROCESSED_HANDLERS_KEY = "nablarch_processed_handlers";

    /** 実行対象となるハンドラリストを組み立てる{@link PipelineListBuilder} */
    private PipelineListBuilder handlerListBuilder;

    /**
     * 例外処理を行う {@link ExceptionHandler}。
     */
    private ExceptionHandler exceptionHandler = new GlobalErrorHandler();

    /**
     * 実行対象となるハンドラリストを組み立てる{@link PipelineListBuilder} を設定する。
     *  
     * @param handlerListBuilder 実行対象となるハンドラリストを組み立てる{@link PipelineListBuilder}
     */
    public void setHandlerListBuilder(
            PipelineListBuilder handlerListBuilder) {
        this.handlerListBuilder = handlerListBuilder;
    }
    
    /**
     * 例外処理を行う {@link ExceptionHandler}を設定する。
     * @param exceptionHandler 例外処理を行う {@link ExceptionHandler}
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    /**
     * 事前処理を実行する。
     * @param context ExecutionContext
     * @return 処理結果
     */
    public Result invokeInbound(ExecutionContext context) {
        try {
            List<Object> handlerList = handlerListBuilder.getHandlerList();
            Set<Object> processedHandlers = new HashSet<Object>();
            context.setRequestScopedVar(PROCESSED_HANDLERS_KEY, processedHandlers);
            
            for (Object object : handlerList) {
                if (object instanceof InboundHandleable) {
                    InboundHandleable handler = (InboundHandleable) object;
    
                    Result ret = handler.handleInbound(context);
                    if (!ret.isSuccess()) {
                        //結果が成功でなかった場合は、処理を中断する。
                        return ret;
                    }
                    
                    processedHandlers.add(handler);
                }
            }
            return new Result.Success();
        } catch (Error e) {
            return handleError(context, e);
        } catch (RuntimeException e) {
            return handleRuntimeException(context, e);
        }
    }

    /**
     * 事前処理を実行する。
     * @param context ExecutionContext
     * @return 処理結果
     */
    public Result invokeOutbound(ExecutionContext context) {
        List<Object> handlerList = new ArrayList<Object>();
        handlerList.addAll(handlerListBuilder.getHandlerList());

        Collections.reverse(handlerList);

        Set<Object> processedHandlers = getProcessedHandlers(context);

        
        Result firstInvalidResult = null;
        Throwable firstThrowable = null;
        // outbound は、リソース開放モレの要因になるため、Inboundで処理したハンドラは全て処理する。
        // 処理結果として返すResultは、最初のResult(一番初めの問題を表すResult)とする。
        for (Object object : handlerList) {
            if (object instanceof OutboundHandleable) {
                OutboundHandleable handler = (OutboundHandleable) object;
                
                if (processedHandlers.contains(handler)) {
                    Result result = null;
                    try {
                        result = handler.handleOutbound(context);
                    } catch (Throwable t) {
                        // 後続のハンドラに対して、例外があったことを通知するためにcontextの状態を変更する。
                        context.setProcessSucceeded(false);
                        try {
                            result = handleException(context, t);
                        } catch (Throwable t1) {
                            if (firstThrowable == null) {
                                firstThrowable = t1;
                            }
                        }
                    }
                    if (result != null && !result.isSuccess() && firstInvalidResult == null) {
                        firstInvalidResult = result;
                    }
                }
            }
        }
        
        // 例外が発生した場合、最初の例外を投げる。
        if (firstThrowable != null) {
            if (firstThrowable instanceof RuntimeException) {
                throw (RuntimeException) firstThrowable;
            } else {
                throw (Error) firstThrowable;
            }
        }
        
        return firstInvalidResult != null ? firstInvalidResult : new Result.Success();
    }

    /**
     * 例外処理を行う
     * @param context ExecutionContext
     * @param t 対象の例外(RuntimeException または Errorのいずれかとなる)
     * @return 処理結果
     */
    protected Result handleException(ExecutionContext context, Throwable t) {
        if (t instanceof RuntimeException) {
            return handleRuntimeException(context, (RuntimeException) t);
        } else {
            return handleError(context, (Error) t);
        }
    }
    
    /**
     * 処理済ハンドラのSetを取得する。
     * 
     * @param context ExecutionContext
     * @return 処理済ハンドラのSet
     */
    protected Set<Object> getProcessedHandlers(
            ExecutionContext context) {
        Set<Object> processedHandlers = context.getRequestScopedVar(PROCESSED_HANDLERS_KEY);
        if (processedHandlers == null) {
            processedHandlers = new HashSet<Object>();
            context.setRequestScopedVar(PROCESSED_HANDLERS_KEY, processedHandlers);
        }
        return processedHandlers;
    }

    /**
     * RuntimeExceptionの例外処理を行う。<br/>
     * 例外をNablarchのハンドラでレスポンスとして処理する場合、
     * このハンドラより外部のハンドラが処理できるレスポンスオブジェクトを返す。
     * 
     * @param context ExecutionContext
     * @param e 例外
     * @return 例外を表すレスポンスオブジェクト
     * @throws RuntimeException 例外を処理できない場合、または付け替えた例外
     */
    protected Result handleRuntimeException(ExecutionContext context,
            RuntimeException e) {
        return exceptionHandler.handleRuntimeException(e, context);
    }

    /**
     * Error の例外処理を行う。<br/>
     * 例外をNablarchのハンドラでレスポンスとして処理する場合、
     * このハンドラより外部のハンドラが処理できるレスポンスオブジェクトを返す。
     * 
     * @param context ExecutionContext
     * @param e 例外
     * @return 例外を表すレスポンスオブジェクト
     * @throws Error 例外を処理できない場合
     * @throws RuntimeException 例外を処理できない場合、または付け替えた例外
     */
    protected Result handleError(ExecutionContext context, Error e)
            throws Error {
        return exceptionHandler.handleError(e, context);
    }
    
}
