package nablarch.fw.invoker;

import nablarch.fw.ExceptionHandler;
import nablarch.fw.ExecutionContext;
import nablarch.fw.InboundHandleable;
import nablarch.fw.OutboundHandleable;
import nablarch.fw.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PipelineInvokerTest {
    private PipelineInvoker target = new PipelineInvoker();

    private BasicPipelineListBuilder listBuilder = new BasicPipelineListBuilder();
    
    
    private final InboundOutboundHandler firstHandler = mock(InboundOutboundHandler.class);
    
    private final InboundOutboundHandler secondHandler = mock(InboundOutboundHandler.class);

    private final InboundHandleable inboundHandleable = mock(InboundHandleable.class);

    private final OutboundHandleable outboundHandleable = mock(OutboundHandleable.class);

    private final ExceptionHandler exceptionHandler = mock(ExceptionHandler.class, RETURNS_DEEP_STUBS);

    private ExecutionContext context = mock(ExecutionContext.class);

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

        // 正常系
        when(firstHandler.handleInbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleInbound(context)).thenReturn(new Result.Success());

        assertTrue(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler).handleInbound(context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();
        assertTrue(captured.contains(firstHandler));
        assertTrue(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundFirstHandlerRuntimeException() {
        // 1つめのハンドラで RuntimeException 発生

        final RuntimeException e = new RuntimeException();
        
        when(firstHandler.handleInbound(context)).thenThrow(e);
        when(secondHandler.handleInbound(context)).thenReturn(new Result.Success());
        when(exceptionHandler.handleRuntimeException(e, context)).thenReturn(new NotSuccess());

        assertFalse(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler, never()).handleInbound(context);
        verify(exceptionHandler, atLeastOnce()).handleRuntimeException(e, context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();

        assertFalse(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundSecondHandlerRuntimeException() {
        // 2つめのハンドラで RuntimeException 発生

        final RuntimeException e = new RuntimeException();
        
        when(firstHandler.handleInbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleInbound(context)).thenThrow(e);
        when(exceptionHandler.handleRuntimeException(e, context)).thenReturn(new NotSuccess());

        assertFalse(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler).handleInbound(context);
        verify(exceptionHandler, atLeastOnce()).handleRuntimeException(e, context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();
        
        assertTrue(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundFirstHandlerError() {

        final Error e = new Error();
        
        // 1つめのハンドラで Error 発生
        when(firstHandler.handleInbound(context)).thenThrow(e);
        when(secondHandler.handleInbound(context)).thenReturn(new Result.Success());
        when(exceptionHandler.handleError(e, context)).thenReturn(new NotSuccess());

        assertFalse(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler, never()).handleInbound(context);
        verify(exceptionHandler, atLeastOnce()).handleError(e, context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();
        
        assertFalse(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundSecondHandlerError() {
        final Error e = new Error();

        // 2つめのハンドラで Error 発生
        when(firstHandler.handleInbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleInbound(context)).thenThrow(e);
        when(exceptionHandler.handleError(e, context)).thenReturn(new NotSuccess());

        assertFalse(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler).handleInbound(context);
        verify(exceptionHandler, atLeastOnce()).handleError(e, context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();
        
        assertTrue(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundFirstHandlerReturnNotSuccess() {
        // 1つめのハンドラが Success 以外を返却
        when(firstHandler.handleInbound(context)).thenReturn(new NotSuccess());
        when(secondHandler.handleInbound(context)).thenReturn(new Result.Success());

        assertFalse(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler, never()).handleInbound(context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();
        
        assertFalse(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    @Test
    public void testHandleInboundSecondHandlerReturnNotSuccess() {
        // 2つめのハンドラが Success 以外を返却
        when(firstHandler.handleInbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleInbound(context)).thenReturn(new NotSuccess());

        assertFalse(target.invokeInbound(context).isSuccess());

        verify(firstHandler).handleInbound(context);
        verify(secondHandler).handleInbound(context);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Set<InboundOutboundHandler>> captor = ArgumentCaptor.forClass(Set.class);

        verify(context).setRequestScopedVar(eq(PipelineInvoker.PROCESSED_HANDLERS_KEY), captor.capture());

        final Set<InboundOutboundHandler> captured = captor.getValue();
        
        assertTrue(captured.contains(firstHandler));
        assertFalse(captured.contains(secondHandler));
    }

    
    @Test
    public void testHandleOutboundNormalCase() {
        
        // 正常系
        when(firstHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleOutbound(context)).thenReturn(new Result.Success());
        
        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertTrue(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);

    }

    @Test
    public void testHandleOutboundProcessedHandlersNotContainsFirstHandler() {
        
        // 1つ目のハンドラが processedHandlers に入っていない場合
        when(firstHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleOutbound(context)).thenReturn(new Result.Success());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertTrue(target.invokeOutbound(context).isSuccess());

        verify(firstHandler, never()).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);

    }

    @Test
    public void testHandleOutboundProcessedHandlersNotContainsSecondHandler() {
        
        // 2つ目のハンドラが processedHandlers に入っていない場合
        when(firstHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleOutbound(context)).thenReturn(new Result.Success());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);
        
        assertTrue(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler, never()).handleOutbound(context);

    }

    @Test
    public void testHandleOutboundFirstHandlerResultNotSuccess() {

        // 1つめのハンドラの処理結果が NotSuccess
        when(firstHandler.handleOutbound(context)).thenReturn(new NotSuccess());
        when(secondHandler.handleOutbound(context)).thenReturn(new Result.Success());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertFalse(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);

    }

    @Test
    public void testHandleOutboundSecondHandlerResultNotSuccess() {

        // 2つめのハンドラの処理結果が NotSuccess
        when(firstHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleOutbound(context)).thenReturn(new NotSuccess());


        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertFalse(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);

    }

    @Test
    public void testHandleOutboundBothHandlerResultNotSuccess() {

        // 1つめ、2つめのハンドラの処理結果が NotSuccess
        when(firstHandler.handleOutbound(context)).thenReturn(new NotSuccess(1));
        when(secondHandler.handleOutbound(context)).thenReturn(new NotSuccess(2));

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        NotSuccess result = (NotSuccess) target.invokeOutbound(context);
        
        // 先に処理される2つ目のハンドラが結果としてもどる。
        assertEquals(2, result.no);

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);

    }

    @Test
    public void testHandleOutboundFirstHandlerRuntimeException() {
        final RuntimeException e = new RuntimeException();

        // 1つめのハンドラでRuntimeException発生
        when(firstHandler.handleOutbound(context)).thenThrow(e);
        when(secondHandler.handleOutbound(context)).thenReturn(new Result.Success());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertFalse(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);
        verify(exceptionHandler, atLeastOnce()).handleRuntimeException(e, context);

    }

    @Test
    public void testHandleOutboundSecondHandlerRuntimeException() {
        final RuntimeException e = new RuntimeException();

        // 1つめのハンドラでRuntimeException発生
        when(firstHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleOutbound(context)).thenThrow(e);

        when(exceptionHandler.handleRuntimeException(e, context)).thenReturn(new NotSuccess());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertFalse(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);
        verify(exceptionHandler, atLeastOnce()).handleRuntimeException(e, context);

    }

    @Test
    public void testHandleOutboundFirstHandlerError() {
        final Error e = new Error();

        // 1つめのハンドラでRuntimeException発生
        when(firstHandler.handleOutbound(context)).thenThrow(e);
        when(secondHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(exceptionHandler.handleError(e, context)).thenReturn(new NotSuccess());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        assertFalse(target.invokeOutbound(context).isSuccess());

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);
        verify(exceptionHandler, atLeastOnce()).handleError(e, context);

    }

    @Test
    public void testHandleOutboundSecondHandlerError() {
        // mockは使わない
        context = spy(new ExecutionContext());
        context.setProcessSucceeded(true);
        final Error e = new Error();

        final AtomicBoolean succeeded = new AtomicBoolean(true);

        // 2つめのハンドラでRuntimeException発生
        when(firstHandler.handleOutbound(context)).then((ctx) -> {
            // 後続のハンドラのOutboundで例外が発生しているので、
            // Contextの状態は正常ではない
            final ExecutionContext context = ctx.getArgument(0);
            succeeded.set(context.isProcessSucceeded());
            return new Result.Success();
        });
        when(secondHandler.handleOutbound(context)).thenThrow(e);
        when(exceptionHandler.handleError(e, context)).thenReturn(new NotSuccess()).thenThrow(e);

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        try {
            assertFalse(target.invokeOutbound(context).isSuccess());
            fail("例外が発生するはず");
        } catch(Error e1) {
            
        }

        assertThat("firstHandlerでの状態は異常状態になっていること", succeeded.get(), is(false));
        assertThat("contextの状態は異常終了状態", context.isProcessSucceeded(), is(false));

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);
        verify(exceptionHandler).handleError(e, context);

    }

    @Test
    public void testHandleOutboundBothHandlerError() {
        final Error e = new Error();

        // 1つめ、2つめのハンドラ両方でRuntimeException発生
        when(firstHandler.handleOutbound(context)).thenThrow(e);
        when(secondHandler.handleOutbound(context)).thenThrow(e);
        when(exceptionHandler.handleError(e, context)).thenReturn(new NotSuccess()).thenThrow(e);

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        try {
            assertFalse(target.invokeOutbound(context).isSuccess());
            fail("例外が発生するはず");
        } catch(Error e1) {
            
        }

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);
        verify(exceptionHandler, atLeastOnce()).handleError(e, context);

    }

    @Test
    public void testHandleOutboundProcessedHandlersNotFound() {
        final Error e = new Error();

        // PipelineInvoker.PROCESSED_HANDLERS_KEY で値が取得できない場合。
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(null);

        assertTrue(target.invokeOutbound(context).isSuccess());

    }

    @Test
    public void testHandleOutboundExceptionHandlerThrowRuntimeException() {
        final Error e = new Error();

        // ExceptionHandlerで例外が発生した場合(ほぼExceptionHandlerにバグがあった場合のみ発生する)
        when(firstHandler.handleOutbound(context)).thenReturn(new Result.Success());
        when(secondHandler.handleOutbound(context)).thenThrow(e);
        when(exceptionHandler.handleError(e, context)).thenThrow(new RuntimeException());

        Set<InboundOutboundHandler> proceeded = new HashSet<InboundOutboundHandler>();
        proceeded.add(firstHandler);
        proceeded.add(secondHandler);
        when(context.getRequestScopedVar(PipelineInvoker.PROCESSED_HANDLERS_KEY)).thenReturn(proceeded);

        try {
            assertFalse(target.invokeOutbound(context).isSuccess());
            fail("例外が発生するはず");
        } catch(RuntimeException e1) {
            
        }

        verify(firstHandler).handleOutbound(context);
        verify(secondHandler).handleOutbound(context);
        verify(exceptionHandler, atLeastOnce()).handleError(e, context);

    }

    @Test
    public void testHandleInboundNotExtendedInboundHandleable() {
        listBuilder.setHandlerList(new ArrayList<Object>() {{
            add(outboundHandleable);
        }});

        assertTrue(target.invokeOutbound(context).isSuccess());

        verify(outboundHandleable, never()).handleOutbound(context);
    }

    @Test
    public void testHandleOutboundNotExtendedOutboundHandleable() {
        listBuilder.setHandlerList(new ArrayList<Object>() {{
            add(inboundHandleable);
        }});

        assertTrue(target.invokeOutbound(context).isSuccess());

        verify(inboundHandleable, never()).handleInbound(context);
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
