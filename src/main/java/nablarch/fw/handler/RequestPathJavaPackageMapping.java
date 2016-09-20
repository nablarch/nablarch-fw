package nablarch.fw.handler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Request;
import nablarch.fw.Result;

/**
 * リクエストパスをJavaパッケージへマッピングすることで動的に委譲先ハンドラを決定するディスパッチハンドラ。
 * <p/>
 * マッピング先Javaパッケージは、本ハンドラの basePackage プロパティに設定する。
 * <p/>
 * リクエストパスを単一のJavaパッケージにマッピングする場合の設定例を以下に示す。
 * <table border="1">
 * <tr bgcolor="#cccccc">
 * <th>本ハンドラの basePackage プロパティ</th>
 * <th>リクエストパス</th>
 * <th>委譲先のハンドラクラス</th>
 * </tr>
 * <tr>
 * <td rowspan=2>nablarch.sample.apps</td>
 * <td>/admin/AdminApp</td>
 * <td>nablarch.sample.apps.admin.AdminApp</td>
 * </tr>
 * <tr>
 * <td>/user/UserApp</td>
 * <td>nablarch.sample.apps.user.UserApp</td>
 * </tr>
 * </table>
 * <p>
 * 委譲先のクラスが存在しない、もしくは、クラスが存在してもハンドラインターフェース {@link nablarch.fw.Handler} を実装していない場合は、
 * 汎用例外 {@link nablarch.fw.Result.NotFound} が送出される。
 * </p>
 * <p>
 * <h3>ベースパスの指定</h3>
 * マッピング元となるリクエストパスのプレフィックスのことを「ベースパス」と呼ぶ。ベースパスは basePath プロパティ に設定する。
 * </p>
 * <p>
 * ベースパスには、画面オンライン処理におけるコンテキストルートを設定することを想定している。
 * （※画面オンライン処理では、実際にはハンドラとして{@link HttpRequestJavaPackageMapping}を使用するが、
 * 実際にディスパッチ処理を行なっているのは、{@link nablarch.fw.web.handler.HttpRequestJavaPackageMapping}から処理を委譲される本ハンドラである）<br/>
 * </p>
 * <p>
 * マッピング処理を行う際には、リクエストパス中のベースパス部分の文字列を削除した上で、マッピング先Javaパッケージへとマッピングする。<br/>
 * 以下に、画面オンライン処理でベースパスを指定し、リクエストパスを単一のJavaパッケージにマッピングする場合の設定例を示す。
 * <table border="1">
 * <tr bgcolor="#cccccc">
 * <th>本ハンドラの basePath プロパティ</th>
 * <th>本ハンドラの basePackage プロパティ</th>
 * <th>リクエストパス</th>
 * <th>委譲先のハンドラクラス</th>
 * </tr>
 * <tr>
 * <td rowspan=2>/webapp/sample</td>
 * <td>nablarch.sample.apps</td>
 * <td>/webapp/sample/admin/AdminApp</td>
 * <td>nablarch.sample.apps.admin.AdminApp</td>
 * </tr>
 * <tr>
 * <td>nablarch.sample.apps</td>
 * <td>/webapp/sample/user/UserApp</td>
 * <td>nablarch.sample.apps.user.UserApp</td>
 * </tr>
 * </table>
 * <br/>
 * なお、ベースパスが指定されていて、かつリクエストパスがそのベースパスに合致しない場合は、汎用例外 {@link nablarch.fw.Result.NotFound} が送出される。
 * </p>
 * </p>
 * <p>
 * <h3>リクエストパスごとのマッピング先Javaパッケージの切り替え</h3>
 * optionalPackageMappingEntries プロパティに設定を行うことで、リクエストパスごとにマッピング先Javaパッケージを切り替えることができる。
 * </p>
 * <p>
 * optionalPackageMappingEntries プロパティには、リクエストパスのパターン（requestPattern プロパティ）とマッピング先Javaパッケージ（basePackage プロパティ）の組み合わせを設定する。<br/>
 * optionalPackageMappingEntries プロパティに設定した順番にリクエストパスのパターンとリクエストパスとのマッチングが行われ、
 * 最初にマッチしたマッピング先Javaパッケージが使用される。 マッチするものが存在しない場合、本ハンドラの basePackage プロパティに設定したマッピング先Javaパッケージが使用される。
 * </p>
 * <p>
 * 以下に、リクエストパスごとにマッピング先Javaパッケージを切り替える場合の設定例を示す。</br>
 * <table border="1">
 * <tr bgcolor="#cccccc">
 * <th>optionalPackageMappingEntriesの requestPattern プロパティ</th>
 * <th>optionalPackageMappingEntriesの basePackage プロパティ</th>
 * <th>リクエストパス</th>
 * <th>委譲先のハンドラクラス</th>
 * </tr>
 * <tr>
 * <td>/admin//</td>
 * <td>nablarch.app1</td>
 * <td>/admin/AdminApp</td>
 * <td>nablarch.app1.admin.AdminApp</td>
 * </tr>
 * <tr>
 * <td>/user//</td>
 * <td>nablarch.app2</td>
 * <td>/user/UserApp</td>
 * <td>nablarch.app2.user.UserApp</td>
 * </tr>
 * </table>
 * <p/>
 * リクエストパスのパターンのマッチングは、リクエストパス中のすべてのドット(.)をスラッシュ(/)に置換してから行う。
 * この仕様は、Nablarch のバッチ処理で過去に使用していたドット区切りのリクエストパス（例： ss01A001.B01AA001Action/B01AA0010）との互換性を保つために存在している。
 * <p/>
 * リクエストパスのパターンの記法についての詳細は{@link nablarch.fw.RequestPathMatchingHelper}を参照すること。
 * 
 * @see Request#getRequestPath()
 * @see nablarch.fw.RequestPathMatchingHelper
 * @author Iwauo Tajima
 */
public class RequestPathJavaPackageMapping
extends DispatchHandler<Request<?>, Object, RequestPathJavaPackageMapping> {
    // ---------------------------------------------------------- Structure
    /** ロガー */
    private static final Logger
        LOGGER = LoggerManager.get(RequestPathJavaPackageMapping.class);
    /** ベースパス <-> Javaパッケージのマッピングを表す正規表現 */
    private static final Pattern MAPPING_RULE = Pattern.compile(
    "((?:[_a-z][_a-zA-Z0-9]*\\.)*)([A-Z][_a-zA-Z0-9]*).*"
    );
    /** マッピング元ベースパス */
    private String basePath = "";
    
    /** マッピング先Javaパッケージ */
    private String basePackage = "";

    /** RequestHandlerEntryでURIに合致したマッピング先Javaパッケージを上書きする場合に使用する、JavaPackageMappingEntryのリスト */
    private List<JavaPackageMappingEntry> optionalPackageMappingEntries;
    
    /** 移譲対象クラス名の接尾辞 */
    private String classNameSuffix = "";
    
    /** 委譲対象クラス名の接頭辞 */ 
    private String classNamePrefix = "";
    
    // ---------------------------------------------------------- Handler I/F
    /**
     * {@inheritDoc}
     * このクラスの実装では、ベースパスとベースパッケージの設定をもとに算出した完全修飾名に一致するリクエストハンドラに対して処理を委譲する。
     * 正確な仕様は以下の通り。
     * <p/>
     * 委譲先のクラス(完全修飾名)の決定は以下の規則に従う。
     * <pre>
     *   1. basePackage の ”.” を ”/” に置換する。
     *   2. リクエストパスの先頭から basePath と一致する部分を basePackage
     *      に置換する。
     *   3. 2.の結果文字列を”/”で分割する。
     *      分割後の各トークンの内、英大文字で始まっているものを委譲先の
     *      クラス名とし、それ以前の各トークンをパッケージ名とみなす。
     *   4. コンテキストクラスパス上に上記のパッケージ及びクラスが実際に
     *      存在していれば、そのクラスを委譲対象とする。
     * </pre>
     * 以下の場合、共通例外{@link nablarch.fw.Result.NotFound}を送出する。
     * <pre>
     *   - ベースパス外からのアクセスであった場合。
     *   - 委譲先のクラスが決定できない、決定できても存在しない場合。
     *   - 委譲先のクラスがHandlerインターフェースを実装していない場合。
     * </pre>
     */
    protected Class<?> getHandlerClass(Request<?> req, ExecutionContext ctx)
    throws ClassNotFoundException {
        if (!req.getRequestPath().startsWith(basePath)) {
            String message = "Couldn't map request.: " + req.getRequestPath();
            
            LOGGER.logInfo(message);
            throw new Result.NotFound(message);
        }
        
        String basePackage = getBasePackage(req, ctx);
        
        String mappedUri = req.getRequestPath()
                              .replaceFirst(basePath, basePackage + ".")
                              .replaceAll("[./]+", ".")
                              .replaceAll("^\\.|\\.$", "");

        Matcher m = MAPPING_RULE.matcher(mappedUri);
        if (!m.matches()) {
            String message = "Couldn't map request.: " + mappedUri;
            LOGGER.logInfo(message);
            throw new Result.NotFound(message);
        }
        String packageName  = (m.group(1) == null) ? "" : m.group(1);
        String className    = classNamePrefix + m.group(2) + classNameSuffix;
        String fqn          = packageName + className;
        
        writeDispatchingClassLog(req, ctx, fqn);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return Class.forName(fqn, true, loader);
    }

    /**
     * マッピング先Javaパッケージを取得する。
     * <p/>
     * optionalPackageMappingEntries プロパティに設定した順番にリクエストパスのパターンとリクエストパスとのマッチングが行われ、 最初にマッチしたJavaパッケージが使用される。 <br/>
     * マッチするものが存在しない場合、またはoptionalPackageMappingEntries プロパティ自体が設定されていない場合、本ハンドラのbasePackage プロパティに設定したJavaパッケージが使用される。
     * 
     * @param  req 入力データ
     * @param  ctx 実行コンテキスト
     * @return マッピング先Javaパッケージ
     */
    protected String getBasePackage(Request<?> req, ExecutionContext ctx) {
        if (optionalPackageMappingEntries != null) {
            for (JavaPackageMappingEntry entry : optionalPackageMappingEntries) {
                boolean appliedTo = entry.getRequestPathMatching().isAppliedTo(
                        req, ctx);
                if (appliedTo) {
                    return entry.getBasePackage();
                }
            }
        }
        return basePackage;
    }

    // ---------------------------------------------------- constructors
    /**
     * デフォルトコンストラクタ。
     * <p/>
     * このメソッドの処理は次のコードと同等である。
     * <pre>
     *   new RequestPathJavaPackageMapping("", "");
     * </pre>
     */
    public RequestPathJavaPackageMapping() {
        this("", "");
    }
    
    /**
     * リクエストパスが、basePathで始まるリクエストを、basePackageで指定された
     * Javaパッケージ配下のリクエストハンドラに委譲するディスパッチャを作成する。
     * 
     * @param basePath   マッピング元ベースURI
     * @param basePackage マッピング先Javaパッケージ
     */
    public RequestPathJavaPackageMapping(String basePath, String basePackage) {
        if (basePath == null || basePackage == null) {
            throw new IllegalArgumentException(
                "The arguments of this constructor must not be null."
            );
        }
        this.basePath    = basePath;
        this.basePackage = basePackage;
    }

    // ---------------------------------------------------------- accessors
    /**
     * マッピング元ベースパスを設定する。
     * 
     * @param basePath マッピング元ベースパス
     * @return JavaPackageMapping
     */
    public RequestPathJavaPackageMapping setBasePath(String basePath) {
        if (basePath == null) {
            throw new IllegalArgumentException(
                "BasePath property must not be null."
            );
        }
        this.basePath = basePath;
        return this;
    }

    /**
     * マッピング先Javaパッケージを設定する。
     * 
     * @param basePackage マッピング先Javaパッケージ
     * @return JavaPackageMapping
     */
    public RequestPathJavaPackageMapping setBasePackage(String basePackage) {
        if (basePackage == null) {
            throw new IllegalArgumentException(
                "basePackage property must not be null."
            );
        }
        this.basePackage = basePackage;
        return this;
    }
    
    /**
     * RequestHandlerEntryでリクエストパスに合致したマッピング先Javaパッケージを上書きする場合に使用する、JavaPackageMappingEntryのリストを設定する。
     * 
     * @param optionalPackageMappingEntries JavaPackageMappingEntryのリスト
     * @return このオブジェクト自体
     */
    public RequestPathJavaPackageMapping setOptionalPackageMappingEntries(List<JavaPackageMappingEntry> optionalPackageMappingEntries) {
        this.optionalPackageMappingEntries = optionalPackageMappingEntries;
        return this;
    }

    /**
     * 委譲対象クラス名の接頭辞となる文字列を設定する。
     * 
     * @param prefix 委譲対象クラス名の接頭辞となる文字列
     * @return このオブジェクト自体
     */
    public RequestPathJavaPackageMapping setClassNamePrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null.");
        }
        classNamePrefix = prefix;
        return this;
    }
    
    /**
     * 委譲対象クラス名の接尾辞となる文字列を設定する。
     * 
     * @param suffix 委譲対象クラス名の接尾辞となる文字列
     * @return このオブジェクト自体
     */
    public RequestPathJavaPackageMapping setClassNameSuffix(String suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("suffix must not be null.");
        }
        classNameSuffix = suffix;
        return this;
    }
    
    
}
