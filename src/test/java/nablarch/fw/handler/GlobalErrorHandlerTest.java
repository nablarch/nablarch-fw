package nablarch.fw.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.invoker.BasicHandlerListBuilder;
import nablarch.fw.invoker.BasicHandlerListInvoker;
import nablarch.fw.results.ServiceUnavailable;
import nablarch.fw.results.Unauthorized;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class GlobalErrorHandlerTest {
    
    @Test
    public void testSuccess() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                return "OK";
            }
        };

        ExecutionContext executionContext = new ExecutionContext();

        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));
        Object result = createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);
        
        assertEquals("OK", result);
    }
    @Test
    public void testHandlingOfOutOfMemoryError() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new OutOfMemoryError();
            }
        };
        
        ExecutionContext executionContext = new ExecutionContext();

        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));
        nablarch.fw.results.InternalError ret = (nablarch.fw.results.InternalError) createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);
        
        assertTrue(ret instanceof nablarch.fw.results.InternalError); // OutOfMemoryErrorは握りつぶされる。
        assertTrue(ret.getCause() instanceof OutOfMemoryError); // OutOfMemoryErrorは握りつぶされる。
        
    }
    
    @Test
    public void testHandlingOfThreadDeath() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new ThreadDeath();
            }
        };

        ExecutionContext executionContext = new ExecutionContext();

        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));
        
        try {
            createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);
            fail();
        } catch (ThreadDeath e) {
            // OK
        }
    }
    
    @Test
    public void testHandlingOfRuntimeException() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new NullPointerException();
            }
        };

        ExecutionContext executionContext = new ExecutionContext();
        

        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));

        nablarch.fw.results.InternalError ret = (nablarch.fw.results.InternalError) createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);

        assertTrue(ret instanceof nablarch.fw.results.InternalError); // OutOfMemoryErrorは握りつぶされる。
        assertTrue(ret.getCause() instanceof NullPointerException); // OutOfMemoryErrorは握りつぶされる。
    }
    
    @Test
    public void testHandlingOfVirtualMachineError() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new InternalError("internal error");
            }
        };

        ExecutionContext executionContext = new ExecutionContext();
        
        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));

        try {
            createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);
            fail();
        } catch (InternalError e) {
            assertThat(e.getMessage(), is("internal error"));
        }

    }
    
    @Test
    public void testHandlingOfNormalError() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new LinkageError();
            }
        };

        ExecutionContext executionContext = new ExecutionContext();
        
        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));

        nablarch.fw.results.InternalError ret 
            = (nablarch.fw.results.InternalError) createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);

        assertTrue(ret instanceof nablarch.fw.results.InternalError);
        assertTrue(ret.getCause() instanceof LinkageError);
    }
    
    @Test
    public void testHandlingOfStackOverflowError() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new StackOverflowError();
            }
        };

        ExecutionContext executionContext = new ExecutionContext();
        
        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));

        nablarch.fw.results.InternalError ret 
            = (nablarch.fw.results.InternalError) createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);


        assertTrue(ret instanceof nablarch.fw.results.InternalError);
        assertTrue(ret.getCause() instanceof StackOverflowError);
    }

    @Test
    public void testServiceError() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new ServiceUnavailable("error");
            }
        };

        ExecutionContext executionContext = new ExecutionContext();
        
        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));

        ServiceUnavailable ret = (ServiceUnavailable) createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);
        assertThat(ret.getMessage(), is("error"));
    }

    @Test
    public void testResultError() {
        ObjectHandler handler = new ObjectHandler() {
            public Object handle(Object req, ExecutionContext ctx) {
                throw new Unauthorized("unauthorized");
            }
        };

        ExecutionContext executionContext = new ExecutionContext();
        
        List<Handler<?, ?>> handlers = new ArrayList<Handler<?,?>>();
        handlers.addAll(Arrays.asList(new GlobalErrorHandler(), handler));

        Unauthorized ret = (Unauthorized) createHandlerInvoker(handlers).invokeHandlerList(null, executionContext);
        assertThat(ret.getMessage(), is("unauthorized"));
    }

    class ObjectHandler implements Handler<Object, Object> {

        @Override
        public Object handle(Object data, ExecutionContext context) {
            return null;
        }
        
    }

    private BasicHandlerListInvoker<Object, Object> createHandlerInvoker(List<Handler<?, ?>> handlers) {
        BasicHandlerListInvoker<Object, Object> invoker = new BasicHandlerListInvoker<Object, Object>();
        BasicHandlerListBuilder<Object> builder = new BasicHandlerListBuilder<Object>();
        builder.setHandlerList(handlers);
        invoker.setHandlerListBuilder(builder);
        return invoker;
    }
}
