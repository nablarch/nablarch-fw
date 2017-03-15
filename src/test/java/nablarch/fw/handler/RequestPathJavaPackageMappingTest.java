package nablarch.fw.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Map;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Request;
import nablarch.fw.RequestHandlerEntry;
import nablarch.fw.Result;

import org.junit.Test;

/**
 * {@link RequestPathJavaPackageMapping}のテスト。
 *
 * @author Masato Inoue
 */
public class RequestPathJavaPackageMappingTest {

    /**
     * mappingEntriesプロパティにリクエストパスのパターン文字列と マッピング先Javaパッケージの関連を保持するクラスを設定することにより、
     * 単一のJavaPackageMappingEntryで複数のリクエストパスへのディスパッチおよび、デフォルトパスへのマッピングへのディスパッチが可能になることのテスト。
     */
    @Test
    public void testMappingEntries() {
        RequestPathJavaPackageMapping mapping = new RequestPathJavaPackageMapping();

        @SuppressWarnings("serial")
        ArrayList<JavaPackageMappingEntry> list =
                new ArrayList<JavaPackageMappingEntry>() {{
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A001/B11AC001Action/R0001*") // setRequestPattern("//R0001*")と同義
                            .setBasePackage("nablarch.fw.handler.dispatch.test1"));
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A001/B11AC001Action//")
                            .setBasePackage("nablarch.fw.handler.dispatch.test2"));
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A001//")
                            .setBasePackage("nablarch.fw.handler.dispatch.test3"));
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A002/test4/B11AC001")
                            .setBasePackage("nablarch.fw.handler.dispatch.test4.ss00A001.B11AC001Action"));
                }};

        mapping.setOptionalPackageMappingEntries(list);
        mapping.setBasePackage("nablarch.fw.handler.dispatch.base"); // mapping entryに合致しない場合に適用されるベースパス

        ExecutionContext ctx = new ExecutionContext();

        // nablarch.fw.handler.dispatch.test1.B11AC001Actionへのマッピング（リクエストIDでのマッピング）
        final MockRequest request = new MockRequest("ss00A001/B11AC001Action/R00010000");
        mapping.handle(request, ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test1.B11AC001Action"));

        // nablarch.fw.handler.dispatch.test2.B11AC001Actionへのマッピング（パッケージでのマッピング）
        mapping.handle(new MockRequest("ss00A001/B11AC001Action/R00020000"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test2.B11AC001Action"));

        // nablarch.fw.handler.dispatch.test3.B11AC002Actionへのマッピング（パッケージでのマッピング）
        mapping.handle(new MockRequest("ss00A001/B11AC002Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test3.B11AC002Action"));

        // nablarch.fw.handler.dispatch.test4.B11AC001Actionへのマッピング（パッケージでのマッピング + ベースパスがActionの完全修飾名）
        mapping.handle(new MockRequest("ss00A002/test4/B11AC001"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test4.B11AC001Action"));

        // nablarch.fw.handler.dispatch.base.B11AC001Actionへのマッピング（どのエントリにも合致しないのでベースパスにディスパッチされる）
        mapping.handle(new MockRequest("ss00A002/B11AC001Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("base.B11AC001Action"));
    }


    /**
     * mappingEntriesが設定されていない場合に通常のベースパスが使用されることのテスト。
     */
    @Test
    public void testBasePath() {
        RequestPathJavaPackageMapping mapping = new RequestPathJavaPackageMapping();

        mapping.setOptionalPackageMappingEntries(null);
        mapping.setBasePackage("nablarch.fw.handler.dispatch.base"); // mapping entryに合致しない場合に適用されるベースパス

        ExecutionContext ctx = new ExecutionContext();

        // nablarch.fw.handler.dispatch.base.B11AC001Actionへのマッピング（どのエントリにも合致しないのでベースパスにディスパッチされる）
        mapping.handle(new MockRequest("ss00A002/B11AC001Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("base.B11AC001Action"));

    }

    /**
     * 設定ファイルのテスト。設定ファイルの内容は{@link #testMappingEntries()}のテストと全く同じ。<br/>
     * RequestHandlerEntryとRequestPathJavaPackageMappingを結合して使用し、
     * 正常に動作することを確認する。
     */
    @Test
    public void testConfig() {

        // テスト用のリポジトリ構築
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/fw/handler/dispatch/config/entryBatch.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);

        RequestHandlerEntry<MockRequest, Result> entry = SystemRepository.get("batchEntry");

        ExecutionContext ctx = new ExecutionContext();

        // nablarch.fw.handler.dispatch.test1.B11AC001Actionへのマッピング（リクエストIDでのマッピング）
        entry.handle(new MockRequest("ss00A001/B11AC001Action/R00010000"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test1.B11AC001Action"));

        // nablarch.fw.handler.dispatch.test2.B11AC001Actionへのマッピング（パッケージでのマッピング）
        entry.handle(new MockRequest("ss00A001/B11AC001Action/R00020000"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test2.B11AC001Action"));

        // nablarch.fw.handler.dispatch.test3.B11AC002Actionへのマッピング（パッケージでのマッピング）
        entry.handle(new MockRequest("ss00A001/B11AC002Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test3.B11AC002Action"));

        // nablarch.fw.handler.dispatch.test4.B11AC001Actionへのマッピング（パッケージでのマッピング + ベースパスがActionの完全修飾名）
        entry.handle(new MockRequest("ss00A002/test4/B11AC001"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test4.B11AC001Action"));

        // nablarch.fw.handler.dispatch.base.B11AC001Actionへのマッピング（どのエントリにも合致しないのでベースパスにディスパッチされる）
        entry.handle(new MockRequest("ss00A002/B11AC001Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("base.B11AC001Action"));
    }

    /**
     * ドット(.)区切りのリクエストパスを、スラッシュ区切りのリクエストパスト同じように解釈できることのテスト。
     */
    @Test
    public void testRegression() {
        RequestPathJavaPackageMapping mapping = new RequestPathJavaPackageMapping();

        @SuppressWarnings("serial")
        ArrayList<JavaPackageMappingEntry> list =
                new ArrayList<JavaPackageMappingEntry>() {{
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A001/B11AC001Action/R0001*") // setRequestPattern("//R0001*")と同義
                            .setBasePackage("nablarch.fw.handler.dispatch.test1"));
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A001/B11AC001Action//")
                            .setBasePackage("nablarch.fw.handler.dispatch.test2"));
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A001//")
                            .setBasePackage("nablarch.fw.handler.dispatch.test3"));
                    add(new JavaPackageMappingEntry()
                            .setRequestPattern("/ss00A002/test4/B11AC001")
                            .setBasePackage("nablarch.fw.handler.dispatch.test4.ss00A001.B11AC001Action"));
                }};

        mapping.setOptionalPackageMappingEntries(list);
        mapping.setBasePackage("nablarch.fw.handler.dispatch.base"); // mapping entryに合致しない場合に適用されるベースパス

        ExecutionContext ctx = new ExecutionContext();

        // nablarch.fw.handler.dispatch.test1.B11AC001Actionへのマッピング（リクエストIDでのマッピング）
        mapping.handle(new MockRequest("ss00A001.B11AC001Action/R00010000"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test1.B11AC001Action"));

        // nablarch.fw.handler.dispatch.test2.B11AC001Actionへのマッピング（パッケージでのマッピング）
        mapping.handle(new MockRequest("ss00A001.B11AC001Action/R00020000"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test2.B11AC001Action"));

        // nablarch.fw.handler.dispatch.test3.B11AC002Actionへのマッピング（パッケージでのマッピング）
        mapping.handle(new MockRequest("ss00A001.B11AC002Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test3.B11AC002Action"));

        // nablarch.fw.handler.dispatch.test4.B11AC001Actionへのマッピング（パッケージでのマッピング + ベースパスがActionの完全修飾名）
        mapping.handle(new MockRequest("ss00A002.test4/B11AC001"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("test4.B11AC001Action"));

        // nablarch.fw.handler.dispatch.base.B11AC001Actionへのマッピング（どのエントリにも合致しないのでベースパスにディスパッチされる）
        mapping.handle(new MockRequest("ss00A002.B11AC001Action"), ctx);
        assertThat(ctx.<String>getRequestScopedVar("executeAction"), is("base.B11AC001Action"));
    }

    private static class MockRequest implements Request<String> {

        private final String requestPath;

        private MockRequest(final String requestPath) {
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
            return null;
        }

        @Override
        public Map<String, String> getParamMap() {
            return null;
        }
    }

}
