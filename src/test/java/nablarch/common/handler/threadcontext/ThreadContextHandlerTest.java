package nablarch.common.handler.threadcontext;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import nablarch.core.ThreadContext;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * {@link ThreadContextHandler}のテストクラス。
 *
 * @author Kiyohito Itoh
 */
public class ThreadContextHandlerTest {

    @Rule
    public SystemRepositoryResource systemRepositoryResource = new SystemRepositoryResource(null);

    @Before
    public void setUp() throws Exception {
        ThreadContext.clear();
    }

    /**
     * リポジトリの設定に従ってThreadContextに値が設定されること。
     * リポジトリの設定では、全てのプロパティを指定する。
     */
    @Test
    public void リクエストパスなしで未ログイン状態の場合は設定ファイルのデフォルト構成でThreadContextが初期化されること() {

        ThreadContextHandler handler = getHandler("thread-context-handler-for-all-settings-test.xml");

        // リクエストパスなし。
        // 未ログイン。
        final HashMap<String, String> param = new HashMap<String, String>();
        param.put("param", "value");
        final MockRequest request = new MockRequest(null, param);

        ExecutionContext ctx = new ExecutionContext()
                .clearHandlers()
                .addHandler(handler)
                .addHandler(new FinalHandler());

        String result = ctx.handleNext(request);

        assertThat(result, is("value"));
        assertThat(ThreadContext.getRequestId(), is(nullValue()));
        assertThat(ThreadContext.getLanguage(), is(Locale.JAPANESE));
        assertThat(ThreadContext.getExecutionId(), is(notNullValue()));
        assertThat(ThreadContext.getUserId(), is("guest"));
        assertThat(ThreadContext.getTimeZone(), is(TimeZone.getTimeZone("America/Los_Angeles")));
    }

    @Test
    public void リクエストパスがありログイン済みの場合その情報でThreadContextが初期化されること() {

        ThreadContextHandler handler = getHandler("thread-context-handler-for-all-settings-test.xml");

        final HashMap<String, String> param = new HashMap<String, String>();
        param.put("param", "値");
        final MockRequest request = new MockRequest("/action/LoginAction/LOGIN00101", param);

        ExecutionContext ctx = new ExecutionContext()
                .clearHandlers()
                .addHandler(handler)
                .addHandler(new FinalHandler())
                .setSessionScopedVar("/user.id", "admin");

        final String result = ctx.handleNext(request);

        assertThat(result, is("値"));
        assertThat(ThreadContext.getRequestId(), is("LOGIN00101"));
        assertThat(ThreadContext.getLanguage(), is(Locale.JAPANESE));
        assertThat(ThreadContext.getExecutionId(), is(notNullValue()));
        assertThat(ThreadContext.getUserId(), is("admin"));
        assertThat(ThreadContext.getTimeZone(), is(TimeZone.getTimeZone("America/Los_Angeles")));
    }

    @Test
    public void デフォルト設定でのリクエストパスなしかつ未ログイン状態の場合はデフォルトの動作となること() throws Exception {
        ThreadContextHandler handler = getHandler("thread-context-handler-for-default-settings-test.xml");

        final ExecutionContext context = new ExecutionContext()
                .clearHandlers()
                .addHandler(handler)
                .addHandler(new FinalHandler());
        final MockRequest request = new MockRequest(null, new HashMap<String, String>() {{
            put("param", "1");
        }});

        final String result = context.handleNext(request);
        assertThat(result, is("1"));
        assertThat(ThreadContext.getRequestId(), is(nullValue()));
        assertThat(ThreadContext.getLanguage(), is(Locale.getDefault()));
        assertThat(ThreadContext.getExecutionId(), is(notNullValue()));
        assertThat(ThreadContext.getUserId(), is(nullValue()));
        assertThat(ThreadContext.getTimeZone(), is(TimeZone.getDefault()));
    }

    @Test
    public void デフォルト設定でのリクエストパスとログインユーザ有りの場合その値でThreadContextが初期化されること() throws Exception {
        ThreadContextHandler handler = getHandler("thread-context-handler-for-default-settings-test.xml");

        final ExecutionContext context = new ExecutionContext()
                .clearHandlers()
                .addHandler(handler)
                .addHandler(new FinalHandler())
                .setSessionScopedVar("USER_ID", "sa");
        final MockRequest request = new MockRequest("/action/LoginAction/index", new HashMap<String, String>() {{
            put("param", "1");
        }});

        final String result = context.handleNext(request);
        assertThat(result, is("1"));
        assertThat(ThreadContext.getRequestId(), is("index"));
        assertThat(ThreadContext.getLanguage(), is(Locale.getDefault()));
        assertThat(ThreadContext.getExecutionId(), is(notNullValue()));
        assertThat(ThreadContext.getUserId(), is("sa"));
    }

    @Test
    public void コンストラクタで指定した設定しに従いThreadContextが初期化されること() throws Exception {
        ThreadContextHandler handler = new ThreadContextHandler(
                new UserIdAttribute() {{
                    setSessionKey("/user.id");
                    setAnonymousId("guest");
                }},
                new RequestIdAttribute(),
                new LanguageAttribute() {{
                    setDefaultLanguage("ja");
                }},
                new ExecutionIdAttribute());

        final MockRequest request = new MockRequest("/action/LoginAction/login", new HashMap<String, String>() {{
            put("param", "val");
        }});

        ExecutionContext ctx = new ExecutionContext()
                .clearHandlers()
                .addHandler(handler)
                .addHandler(new FinalHandler())
                .setSessionScopedVar("/user.id", "guest");

        final String result = ctx.handleNext(request);
        assertThat(result, is("val"));
        assertThat(ThreadContext.getRequestId(), is("/action/LoginAction/login"));
        assertThat(ThreadContext.getLanguage(), is(Locale.JAPANESE));
        assertThat(ThreadContext.getExecutionId(), is(notNullValue()));
        assertThat(ThreadContext.getUserId(), is("guest"));
    }

    private static class MockRequest implements Request<String> {

        private final Map<String, String> param;

        private final String requestPath;

        private MockRequest(final String requestPath, final Map<String, String> param) {
            this.param = param;
            this.requestPath = requestPath;
        }

        @Override
        public String getRequestPath() {
            return requestPath;
        }

        @Override
        public Request<String> setRequestPath(final String s) {
            throw new UnsupportedOperationException("setRequestPath");
        }

        @Override
        public String getParam(final String s) {
            return param.get(s);
        }

        @Override
        public Map<String, String> getParamMap() {
            return param;
        }
    }

    private static class FinalHandler implements Handler<Request<String>, String> {

        @Override
        public String handle(final Request<String> request, final ExecutionContext context) {
            return request.getParam("param");
        }
    }

    private ThreadContextHandler getHandler(String filename) {
        String path = "classpath:nablarch/common/handler/threadcontext/" + filename;
        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader(path)));
        return (ThreadContextHandler) SystemRepository.getObject("threadContextHandler");
    }
}
