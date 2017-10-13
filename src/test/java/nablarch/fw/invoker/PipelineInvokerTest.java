package nablarch.fw.invoker;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import nablarch.fw.ExceptionHandler;
import nablarch.fw.ExecutionContext;
import nablarch.fw.InboundHandleable;
import nablarch.fw.OutboundHandleable;
import nablarch.fw.Result;

import org.junit.Before;
import org.junit.Test;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

public class PipelineInvokerTest {
    private PipelineInvoker target = new PipelineInvoker();

    private BasicPipelineListBuilder listBuilder = new BasicPipelineListBuilder();
    
    
    @Mocked
    private InboundOutboundHandler firstHandler;
    
    @Mocked
    private InboundOutboundHandler secondHandler;

    @Mocked
    private InboundHandleable inboundHandleable;

    @Mocked
    private OutboundHandleable outboundHandleable;

    @Mocked
    private ExceptionHandler exceptionHandler;

    @Injectable
    private ExecutionContext context;

    @Before
    public void setup() {

        List<Object> handlerList = new ArrayList<Object>();
        handlerList.add(firstHandler);
        handlerList.add(secondHandler);
        
        listBuilder.setHandlerList(handlerList);
        target.setHandlerListBuilder(listBuilder);
        target.setExceptionHandler(exceptionHandler);
    }
    
    @Test
    public void testHandleInboundNormalCase() {

        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();
        // 正常系
        new Expectations() {{
            firstHandler.handleInbound(context);
            result = new Result.Success();
            secondHandler.handleInbound(context);
            result = new Result.Success();
            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
        }};

        assertTrue(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 1;
        }};

        assertTrue(captured.get(0).contains(firstHandler));
        assertTrue(captured.get(0).contains(secondHandler));
    }

    @Test
    public void testHandleInboundFirstHandlerRuntimeException() {
        // 1つめのハンドラで RuntimeException 発生

        final RuntimeException e = new RuntimeException();
        
        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();

        new Expectations() {{
            firstHandler.handleInbound(context);
            result = e;
            secondHandler.handleInbound(context);
            result = new Result.Success();
            minTimes = 0;
            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
            
            exceptionHandler.handleRuntimeException(e, context);
            result = new NotSuccess();
        }};

        assertFalse(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 0;
            exceptionHandler.handleRuntimeException(e, context);
        }};

        assertFalse(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundSecondHandlerRuntimeException() {
        // 2つめのハンドラで RuntimeException 発生

        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();

        final RuntimeException e = new RuntimeException();
        new Expectations() {{
            firstHandler.handleInbound(context);
            result = new Result.Success();
            secondHandler.handleInbound(context);
            result = e;
            
            exceptionHandler.handleRuntimeException(e, context);
            result = new NotSuccess();

            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
        }};

        assertFalse(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 1;
            exceptionHandler.handleRuntimeException(e, context);
        }};

        assertTrue(captured.get(0).contains(firstHandler));
        assertFalse(captured.get(0).contains(secondHandler));
    }

    @Test
    public void testHandleInboundFirstHandlerError() {

        final Error e = new Error();
        
        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();
        
        // 1つめのハンドラで Error 発生
        new Expectations() {{
            firstHandler.handleInbound(context);
            result = e;
            secondHandler.handleInbound(context);
            minTimes = 0;
            result = new Result.Success();
            
            exceptionHandler.handleError(e, context);
            result = new NotSuccess();

            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
        }};

        assertFalse(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 0;
            exceptionHandler.handleError(e, context);
        }};

        assertFalse(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundSecondHandlerError() {
        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();

        final Error e = new Error();

        // 2つめのハンドラで Error 発生
        new Expectations() {{
            firstHandler.handleInbound(context);
            result = new Result.Success();
            secondHandler.handleInbound(context);
            result = e;

            exceptionHandler.handleError(e, context);
            result = new NotSuccess();

            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
        }};

        assertFalse(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 1;
            exceptionHandler.handleError(e, context);
        }};

        assertTrue(captured.get(0).contains(firstHandler));
        assertFalse(captured.get(0).contains(secondHandler));
    }

    @Test
    public void testHandleInboundFirstHandlerReturnNotSuccess() {
        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();

        // 1つめのハンドラが Success 以外を返却
        new Expectations() {{
            firstHandler.handleInbound(context);
            result = new NotSuccess();
            secondHandler.handleInbound(context);
            minTimes = 0;
            result = new Result.Success();
            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
        }};

        assertFalse(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 0;
        }};

        assertFalse(captured.get(0).contains(firstHandler));
        assertFalse(captured.get(0).contains(secondHandler));
    }

    @Test
    public void testHandleInboundSecondHandlerReturnNotSuccess() {
        final List<Set<InboundOutboundHandler>> captured = new ArrayList<Set<InboundOutboundHandler>>();

        // 2つめのハンドラが Success 以外を返却
        new Expectations() {{
            firstHandler.handleInbound(context);
            result = new Result.Success();
            secondHandler.handleInbound(context);
            result = new NotSuccess();
            context.setRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY, withCapture(captured));
        }};

        assertFalse(target.invokeInbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleInbound(context);
            times = 1;
            secondHandler.handleInbound(context);
            times = 1;
        }};

        assertTrue(captured.get(0).contains(firstHandler));
        assertFalse(captured.get(0).contains(secondHandler));
    }

    
    @Test
    public void testHandleOutboundNormalCase() {
        
        // 正常系
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new Result.Success();
            secondHandler.handleOutbound(context);
            result = new Result.Success();
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertTrue(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
        }};

    }

    @Test
    public void testHandleOutboundProcessedHandlersNotContainsFirstHandler() {
        
        // 1つ目のハンドラが processedHandlers に入っていない場合
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new Result.Success();
            minTimes = 0;
            secondHandler.handleOutbound(context);
            result = new Result.Success();
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertTrue(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 0;
            secondHandler.handleOutbound(context);
            times = 1;
        }};

    }

    @Test
    public void testHandleOutboundProcessedHandlersNotContainsSecondHandler() {
        
        // 2つ目のハンドラが processedHandlers に入っていない場合
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new Result.Success();
            secondHandler.handleOutbound(context);
            result = new Result.Success();
            minTimes = 0;
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            result = proceeded;
        }};

        assertTrue(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 0;
        }};

    }

    @Test
    public void testHandleOutboundFirstHandlerResultNotSuccess() {

        // 1つめのハンドラの処理結果が NotSuccess
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new NotSuccess();
            secondHandler.handleOutbound(context);
            result = new Result.Success();

            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertFalse(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
        }};

    }

    @Test
    public void testHandleOutboundSecondHandlerResultNotSuccess() {

        // 2つめのハンドラの処理結果が NotSuccess
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new Result.Success();
            secondHandler.handleOutbound(context);
            result = new NotSuccess();

            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertFalse(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
        }};

    }

    @Test
    public void testHandleOutboundBothHandlerResultNotSuccess() {

        // 1つめ、2つめのハンドラの処理結果が NotSuccess
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new NotSuccess(1);
            secondHandler.handleOutbound(context);
            result = new NotSuccess(2);

            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        NotSuccess result = (NotSuccess) target.invokeOutbound(context);
        
        // 先に処理される2つ目のハンドラが結果としてもどる。
        assertEquals(2, result.no);

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
        }};

    }

    @Test
    public void testHandleOutboundFirstHandlerRuntimeException() {
        final RuntimeException e = new RuntimeException();

        // 1つめのハンドラでRuntimeException発生
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = e;
            secondHandler.handleOutbound(context);
            result = new Result.Success();

            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertFalse(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
            exceptionHandler.handleRuntimeException(e, context);
        }};

    }

    @Test
    public void testHandleOutboundSecondHandlerRuntimeException() {
        final RuntimeException e = new RuntimeException();

        // 1つめのハンドラでRuntimeException発生
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new Result.Success();
            secondHandler.handleOutbound(context);
            result = e;

            exceptionHandler.handleRuntimeException(e, context);
            result = new NotSuccess();
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertFalse(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
            exceptionHandler.handleRuntimeException(e, context);
        }};

    }

    @Test
    public void testHandleOutboundFirstHandlerError() {
        final Error e = new Error();

        // 1つめのハンドラでRuntimeException発生
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = e;
            secondHandler.handleOutbound(context);
            result = new Result.Success();

            exceptionHandler.handleError(e, context);
            result = new NotSuccess();
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        assertFalse(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
            exceptionHandler.handleError(e, context);
        }};

    }

    @Test
    public void testHandleOutboundSecondHandlerError() {
        // mockは使わない
        context = new ExecutionContext();
        context.setProcessSucceeded(true);
        final Error e = new Error();

        final AtomicBoolean succeeded = new AtomicBoolean(true);

        // 2つめのハンドラでRuntimeException発生
        new Expectations(context) {{
            firstHandler.handleOutbound(context);
            result = new Delegate<Result>() {
                public Result delegate(ExecutionContext context) {
                    // 後続のハンドラのOutboundで例外が発生しているので、
                    // Contextの状態は正常ではない
                    succeeded.set(context.isProcessSucceeded());
                    return new Result.Success();
                }
            };
            secondHandler.handleOutbound(context);
            result = e;

            exceptionHandler.handleError(e, context);
            result = new NotSuccess();
            
            exceptionHandler.handleError(e, context);
            result = e;
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        try {
            assertFalse(target.invokeOutbound(context).isSuccess());
            fail("例外が発生するはず");
        } catch(Error e1) {
            
        }

        assertThat("firstHandlerでの状態は異常状態になっていること", succeeded.get(), is(false));
        assertThat("contextの状態は異常終了状態", context.isProcessSucceeded(), is(false));

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
            exceptionHandler.handleError(e, context);
            times = 1;
        }};

    }

    @Test
    public void testHandleOutboundBothHandlerError() {
        final Error e = new Error();

        // 1つめ、2つめのハンドラ両方でRuntimeException発生
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = e;
            secondHandler.handleOutbound(context);
            result = e;

            exceptionHandler.handleError(e, context);
            result = new NotSuccess();
            
            exceptionHandler.handleError(e, context);
            result = e;
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        try {
            assertFalse(target.invokeOutbound(context).isSuccess());
            fail("例外が発生するはず");
        } catch(Error e1) {
            
        }

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
            exceptionHandler.handleError(e, context);
        }};

    }

    @Test
    public void testHandleOutboundProcessedHandlersNotFound() {
        final Error e = new Error();

        // PipelineInvoker.PROCESSED_HANDLERS_KEY で値が取得できない場合。
        new Expectations() {{
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            result = null;
        }};

        assertTrue(target.invokeOutbound(context).isSuccess());

    }

    @Test
    public void testHandleOutboundExceptionHandlerThrowRuntimeException() {
        final Error e = new Error();

        // ExceptionHandlerで例外が発生した場合(ほぼExceptionHandlerにバグがあった場合のみ発生する)
        new Expectations() {{
            firstHandler.handleOutbound(context);
            result = new Result.Success();
            secondHandler.handleOutbound(context);
            result = e;

            exceptionHandler.handleError(e, context);
            result = new NotSuccess();
            
            exceptionHandler.handleError(e, context);
            result = new RuntimeException();
            
            context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY);
            Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
            proceeded.add(firstHandler);
            proceeded.add(secondHandler);
            result = proceeded;
        }};

        try {
            assertFalse(target.invokeOutbound(context).isSuccess());
            fail("例外が発生するはず");
        } catch(RuntimeException e1) {
            
        }

        new Verifications() {{
            firstHandler.handleOutbound(context);
            times = 1;
            secondHandler.handleOutbound(context);
            times = 1;
            exceptionHandler.handleError(e, context);
        }};

    }

    @Test
    public void testHandleInboundNotExtendedInboundHandleable() {
        listBuilder.setHandlerList(new ArrayList<Object>() {{
            add(outboundHandleable);
        }});

        assertTrue(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            outboundHandleable.handleOutbound(context);
            times = 0;
        }};
    }

    @Test
    public void testHandleOutboundNotExtendedOutboundHandleable() {
        listBuilder.setHandlerList(new ArrayList<Object>() {{
            add(inboundHandleable);
        }});

        assertTrue(target.invokeOutbound(context).isSuccess());

        new Verifications() {{
            inboundHandleable.handleInbound(context);
            times = 0;
        }};
    }

    private static class NotSuccess implements Result {
        
        private int no = 0;
        public NotSuccess() {
        }

        public NotSuccess(int no) {
            this.no = no;
        }
        @Override
        public int getStatusCode() {
            return 500;
        }

        @Override
        public String getMessage() {
            return "not success";
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
        
    }

    private interface InboundOutboundHandler extends InboundHandleable,
            OutboundHandleable {
    }
}
