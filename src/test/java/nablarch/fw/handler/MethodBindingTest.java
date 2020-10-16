package nablarch.fw.handler;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * {@link MethodBinding}の単体テスト。
 *
 * @author Tanaka Tomoyuki
 */
public class MethodBindingTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MockAction delegate = new MockAction("RETURN");
    private ExecutionContext context = new ExecutionContext();

    @Test
    public void testDelegateMethod() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "hello");

        String returnValue = sut.handle("requestValue", context);

        assertThat(delegate.request, is("requestValue"));
        assertThat(delegate.context, is(sameInstance(context)));
        assertThat(returnValue, is(delegate.returnValue));
    }

    @Test
    public void testThrowsExceptionIfMethodNotFound() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "notExist");

        exception.expect(Result.NotFound.class);
        exception.expectMessage("Couldn't find method to delegate.: REQUEST");

        sut.handle("REQUEST", context);
    }

    @Test
    public void testThrowsExceptionIfIllegalAccess() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "privateMethod");

        exception.expect(RuntimeException.class);
        exception.expectCause(Matchers.<Throwable>instanceOf(IllegalAccessException.class));

        sut.handle("REQUEST", context);
    }

    @Test
    public void testRuntimeExceptionThrownByDelegateMethodIsRethrow() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "throwsRuntimeException");

        exception.expect(NullPointerException.class);
        exception.expectMessage("test null");

        sut.handle("REQUEST", context);
    }

    @Test
    public void testErrorThrownByDelegateMethodIsRethrow() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "throwsError");

        exception.expect(StackOverflowError.class);
        exception.expectMessage("test stackoverflow");

        sut.handle("REQUEST", context);
    }

    @Test
    public void testExceptionThrownByDelegateMethodIsThrownWrappedByRuntimeException() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "throwsException");

        exception.expect(RuntimeException.class);
        exception.expectCause(Matchers.<Throwable>instanceOf(IOException.class));

        sut.handle("REQUEST", context);
    }

    @Test
    public void testGetDelegates() {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        List<Object> result = sut.getDelegates("nouse", context);

        assertThat(result, contains((Object)delegate));
    }

    @Test
    public void testGetHandleMethod() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method handleMethod = sut.getHandleMethod("HELLO");

        Method helloMethod = MockAction.class.getMethod("hello", String.class, ExecutionContext.class);
        assertThat(handleMethod, is(helloMethod));
    }

    @Test
    public void testReturnNullIfHandleMethodIsNotFound() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method handleMethod = sut.getHandleMethod("notHandleMethod");

        assertThat(handleMethod, is(nullValue()));
    }

    @Test
    public void testHandleMethodOfAnonymousClassBecomeToAccessible() {
        Object delegate = new Object() {
            public String handle(String request, ExecutionContext context) {
                return "anonymous class";
            }
        };

        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method handleMethod = sut.getHandleMethod("handle");
        assertThat(handleMethod.isAccessible(), is(true));
    }

    @Test
    public void testStaticMethodIsNotHandleMethod() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method method = HandleMethodPatternTestAction.class.getMethod("staticMethod", String.class, ExecutionContext.class);

        assertThat(sut.qualifiesAsHandler(method), is(false));
    }

    @Test
    public void testPrivateMethodIsNotHandleMethod() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method method = HandleMethodPatternTestAction.class.getDeclaredMethod("privateMethod", String.class, ExecutionContext.class);

        assertThat(sut.qualifiesAsHandler(method), is(false));
    }

    @Test
    public void testMethodIsNotHandleMethodIfNumberOfArgumentsIsNotTwo() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method method = HandleMethodPatternTestAction.class.getMethod("threeArguments", String.class, ExecutionContext.class, int.class);

        assertThat(sut.qualifiesAsHandler(method), is(false));
    }

    @Test
    public void testMethodIsNotMethodHandleIfSecondArgumentIsNotExecutionContext() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate);

        Method method = HandleMethodPatternTestAction.class.getMethod("notExecutionContext", String.class, int.class);

        assertThat(sut.qualifiesAsHandler(method), is(false));
    }

    @Test
    public void testAssertionErrorIfDelegateIsNull() {
        exception.expect(AssertionError.class);
        new TestMethodBinding(null, "nouse");
    }

    @Test
    public void testDelegateClassAndMethodAreSavedInRequestScope() throws Exception {
        MethodBinding<String, String> sut = new TestMethodBinding(delegate, "hello");

        sut.handle("REQUEST", context);

        Class<?> clazz = context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS);
        assertThat(clazz, is((Object)delegate.getClass()));

        Method method = context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD);
        Method helloMethod = delegate.getClass().getMethod("hello", String.class, ExecutionContext.class);
        assertThat(method, is(helloMethod));
    }

    public static class TestMethodBinding extends MethodBinding<String, String> {
        private final String methodName;

        public TestMethodBinding(Object delegate) {
            this(delegate, null);
        }

        public TestMethodBinding(Object delegate, String methodName) {
            super(delegate);
            this.methodName = methodName;
        }

        @Override
        protected Method getMethodBoundTo(String s, ExecutionContext ctx) {
            try {
                return MockAction.class.getDeclaredMethod(methodName, String.class, ExecutionContext.class);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }

    public static class MockAction {
        private final String returnValue;
        private String request;
        private ExecutionContext context;

        public MockAction(String returnValue) {
            this.returnValue = returnValue;
        }

        public String hello(String request, ExecutionContext context) {
            this.request = request;
            this.context = context;
            return returnValue;
        }

        public void notHandleMethod() {}

        private void privateMethod(String request, ExecutionContext context) {}

        public String throwsRuntimeException(String request, ExecutionContext context) {
            throw new NullPointerException("test null");
        }

        public String throwsError(String request, ExecutionContext context) {
            throw new StackOverflowError("test stackoverflow");
        }

        public String throwsException(String request, ExecutionContext context) throws IOException {
            throw new IOException("test io");
        }
    }

    public static class HandleMethodPatternTestAction {
        public static String staticMethod(String request, ExecutionContext context) {
            return null;
        }

        private String privateMethod(String request, ExecutionContext context) {
            return null;
        }

        public String threeArguments(String request, ExecutionContext context, int integer) {
            return null;
        }

        public String notExecutionContext(String request, int integer) {
            return null;
        }
    }
}