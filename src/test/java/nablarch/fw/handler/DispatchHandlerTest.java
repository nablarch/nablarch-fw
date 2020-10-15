package nablarch.fw.handler;

import nablarch.fw.*;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * {@link DispatchHandler}の単体テスト。
 *
 * @author Tanaka Tomoyuki
 */
public class DispatchHandlerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ExecutionContext context = new ExecutionContext();

    @Before
    public void setup() {
        OnMemoryLogWriter.clear();
    }

    @Test
    public void testHandle() {
        TestDispatchHandler sut = new TestDispatchHandler(TestHandler.class);

        String returnValue = sut.handle("REQUEST", context);

        assertThat(returnValue, is("TEST_HANDLER"));
    }

    @Test
    public void testThrowsExceptionIfHandlerClassNotFound() {
        DispatchHandler<String, String, ?> sut = new DispatchHandler<String, String, DispatchHandler<String, String, ?>>() {

            @Override
            protected Class<?> getHandlerClass(String input, ExecutionContext context) throws ClassNotFoundException {
                throw new ClassNotFoundException("test class not found");
            }
        };

        exception.expect(Result.NotFound.class);
        exception.expectCause(is(Matchers.<Throwable>instanceOf(ClassNotFoundException.class)));

        sut.handle("REQUEST", context);
    }

    @Test
    public void testThrowsExceptionIfDelegateClassCouldNotInstantiate() {
        TestDispatchHandler sut = new TestDispatchHandler(NoDefaultConstructor.class);

        exception.expect(RuntimeException.class);
        exception.expectCause(is(Matchers.<Throwable>instanceOf(InstantiationException.class)));

        sut.handle("REQUEST", context);
    }

    @Test
    public void testThrowsExceptionIfDelegateClassConstructorIsNotAccessible() {
        TestDispatchHandler sut = new TestDispatchHandler(PrivateConstructor.class);

        exception.expect(RuntimeException.class);
        exception.expectCause(is(Matchers.<Throwable>instanceOf(IllegalAccessException.class)));

        sut.handle("REQUEST", context);
    }

    @Test
    public void testThrowsExceptionIfHandlerCloudNotBeCreated() {
        TestDispatchHandler sut = new TestDispatchHandler(NotHandler.class);

        exception.expect(Result.NotFound.class);
        exception.expectMessage("Couldn't instantiate handler.: " + NotHandler.class.getName());

        sut.handle("REQUEST", context);
    }

    @Test
    public void testDelegateHandlerInsertTopOfHandlerQueueIfImmediateIsTrue() {
        TestDispatchHandler sut = new TestDispatchHandler(TestHandler.class);
        sut.setImmediate(true);

        MockHandler fooHandler = new MockHandler("foo");
        MockHandler barHandler = new MockHandler("bar");
        context.addHandler(fooHandler);
        context.addHandler(barHandler);

        String returnValue = sut.handle("REQUEST", context);

        assertThat(fooHandler.invoked, is(false));
        assertThat(barHandler.invoked, is(false));

        assertThat(returnValue, is("TEST_HANDLER"));
    }

    @Test
    public void testDelegateHandlerInsertBottomOfHandlerQueueIfImmediateIsFalse() {
        TestDispatchHandler sut = new TestDispatchHandler(TestHandler.class);
        sut.setImmediate(false);

        MockHandler fooHandler = new MockHandler("foo");
        MockHandler barHandler = new MockHandler("bar");
        context.addHandler(fooHandler);
        context.addHandler(barHandler);

        String returnValue = sut.handle("REQUEST", context);

        assertThat(fooHandler.invoked, is(true));
        assertThat(barHandler.invoked, is(false));

        assertThat(returnValue, is("foo"));

        assertThat(context.getHandlerQueue(), contains(
            instanceOf(MockHandler.class),
            instanceOf(TestHandler.class)
        ));
    }

    @Test
    public void testSetDelegateFactory() {
        TestDispatchHandler sut = new TestDispatchHandler(TestHandler.class);
        sut.setDelegateFactory(new DelegateFactory() {
            @Override
            public Object create(Class<?> clazz) {
                return new TestHandler("CREATE_BY_CUSTOM_DELEGATE_FACTORY");
            }
        });

        String returnValue = sut.handle("REQUEST", context);

        assertThat(returnValue, is("CREATE_BY_CUSTOM_DELEGATE_FACTORY"));
    }

    @Test
    public void testMethodBinderCreateNextHandlerIfDelegateIsNotHandlerAndContextHasMethodBinder() {
        TestDispatchHandler sut = new TestDispatchHandler(NotHandler.class);
        context.setMethodBinder(new MethodBinder<String, String>() {
            @Override
            public HandlerWrapper<String, String> bind(Object o) {
                return new HandlerWrapper<String, String>() {
                    @Override
                    public List<Object> getDelegates(String s, ExecutionContext executionContext) {
                        return null;
                    }

                    @Override
                    public String handle(String s, ExecutionContext executionContext) {
                        return "CUSTOM_METHOD_BINDER";
                    }
                };
            }
        });

        String returnValue = sut.handle("REQUEST", context);

        assertThat(returnValue, is("CUSTOM_METHOD_BINDER"));
    }

    @Test
    public void testWriteDispatchingClassLogIsNoop() {
        TestDispatchHandler sut = new TestDispatchHandler(TestHandler.class);
        sut.writeDispatchingClassLog(null, null, null);

        List<String> memory = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(memory, is(Matchers.<String>emptyIterable()));
    }

    @Test
    public void testHandlerClassAndMethodAreSavedInRequestScope() throws Exception {
        TestDispatchHandler sut = new TestDispatchHandler(TestHandler.class);
        sut.handle("REQUEST", context);

        Class<TestHandler> clazz = context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS);
        assertThat(clazz, sameInstance(TestHandler.class));

        Method method = context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD);
        Method handleMethod = TestHandler.class.getMethod("handle", Object.class, ExecutionContext.class);
        assertThat(method, is(handleMethod));
    }

    public static class TestDispatchHandler extends DispatchHandler<String, String, TestDispatchHandler> {
        private final Class<?> handlerClass;

        public TestDispatchHandler(Class<?> handlerClass) {
            this.handlerClass = handlerClass;
        }

        @Override
        protected Class<?> getHandlerClass(String input, ExecutionContext context) throws ClassNotFoundException {
            return handlerClass;
        }
    }

    public static class TestHandler implements Handler<String, String> {
        private final String returnValue;

        public TestHandler() {
            this("TEST_HANDLER");
        }

        public TestHandler(String returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public String handle(String request, ExecutionContext context) {
            return returnValue;
        }
    }

    public static class NoDefaultConstructor {
        public NoDefaultConstructor(String arg) {}
    }

    public static class PrivateConstructor {
        private PrivateConstructor() {}
    }

    public static class NotHandler {}

    public static class MockHandler implements Handler<String, String> {
        private final String returnValue;
        private boolean invoked;

        public MockHandler(String returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public String handle(String s, ExecutionContext executionContext) {
            invoked = true;
            return returnValue;
        }
    }
}