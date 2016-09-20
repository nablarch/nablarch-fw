package nablarch.fw.handler;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.log.app.FailureLogUtil;
import nablarch.fw.ExceptionHandler;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;
import nablarch.fw.Result;
import nablarch.fw.results.ServiceError;


/**
 * 異例処理用例外ハンドラ。
 * <p/>
 * このハンドラは、リクエストコントローラの直後に配置され、
 * ハンドラキュー上のどのハンドラでも捕捉されなかった例外に対して
 * 最終的に処理を行う責務を持ったハンドラである。
 * <p/>
 * ほとんどのエラーは各処理方式に準じた例外ハンドラーにより捕捉されるが、
 * それらのハンドラが捕捉しないエラー、もしくは、それらのハンドラ以降の
 * 処理で発生したエラーが対象となる。
 * <p/>
 * このハンドラが例外処理として行うのは以下の2点である。
 * 1. ログ出力
 * 2. コントローラに対する例外のリスロー
 * (コントローラ自体の処理継続が不可能な致命的エラーの場合。)
 *
 * @author Iwauo Tajima <iwauo@tis.co.jp>
 */
public class GlobalErrorHandler implements Handler<Request<?>, Object>, ExceptionHandler {

    /** ロガー */
    private static final Logger
            LOGGER = LoggerManager.get(GlobalErrorHandler.class);

    /** {@inheritDoc} */
    public Object handle(Request<?> req, ExecutionContext context) {

        try {
            return context.handleNext(req);
        } catch (Error e) {
            return handleError(e, context);
        } catch (RuntimeException e) {
            return handleRuntimeException(e, context);
        }
            
    }

    @Override
    public Result handleError(Error e, ExecutionContext context) throws Error, RuntimeException {
        if (e instanceof ThreadDeath) {
            // Thread.stop()で止められた場合に発生。
            // 一応Infoログだけ出してからリスロー
            LOGGER.logInfo("Uncaught error: ", e);
            throw e;
    
        } else if (e instanceof StackOverflowError) {
            // 無限ループバグの可能性が高いので通常エラー扱い。
            FailureLogUtil.logFatal(e, context.getDataProcessedWhenThrown(e), null);
    
            return new nablarch.fw.results.InternalError(e);
    
        } else if (e instanceof OutOfMemoryError) {
            // エラー出力にメッセージを出力する。
            // その後、もし可能であればログを出力する。
            // 発生した例外はリスローせずに正常終了する。
            System.err.println("OutOfMemoryError occurred: " + e.getMessage());
            try {
                FailureLogUtil.logFatal(e, context.getDataProcessedWhenThrown(e), null);
    
            } catch (Throwable ignored) {
                LOGGER.logDebug("couldn't write log. : ", e);
            }
    
            return new nablarch.fw.results.InternalError(e);
    
        } else if (e instanceof VirtualMachineError) {
            // StackOverflowError/OutOfMemoryError以外のVMエラーは
            // Fatalログをはいてリスロー。
            FailureLogUtil.logFatal(e, context.getDataProcessedWhenThrown(e), null);
            throw e;
    
    
        } else {
            // 通常エラー扱い。
            FailureLogUtil.logFatal(e, context.getDataProcessedWhenThrown(e), null);
            return new nablarch.fw.results.InternalError(e);
        }
    }

    @Override
    public Result handleRuntimeException(RuntimeException e, ExecutionContext context)
            throws RuntimeException {
        if (e instanceof ServiceError) {
            ServiceError error = (ServiceError) e;
            // 運用ログを出力する。
            error.writeLog(context);
            return error;
            
        } else if (e instanceof Result.Error){
            Result.Error error = (nablarch.fw.Result.Error) e;
            // ServiceError以外のResult.Error
            FailureLogUtil.logFatal(error, context.getDataProcessedWhenThrown(error), null);
            return error;

        } else {
            // 通常エラー扱い。
            FailureLogUtil.logFatal(e, context.getDataProcessedWhenThrown(e), null);
            return new nablarch.fw.results.InternalError(e);
        }
    }
}
