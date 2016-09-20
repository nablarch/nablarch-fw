package nablarch.fw.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nablarch.core.ThreadContext;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;

/**
 * 置換ルール。
 * 
 * @param <TData> 処理対象オブジェクトの型
 * @param <TSelf> 継承型
 * 
 * @author Iwauo Tajima
 */
@Published(tag = "architect")
public abstract class RewriteRule<TData, TSelf> {    
    // --------------------------------------------------- inner classes
    /** 置換ルールの適用条件 */
    private static final class Condition {
        /** 変数種別 */
        private final String  paramType;
        /** 変数名 */
        private final String  paramName;
        /** パターン */
        private final Pattern pattern;
        /** 否定 */
        private final boolean invertMatch;
        
        /**
         * コンストラクタ。
         * @param line 条件定義文字列
         */
        private Condition(String line) {
            Matcher m = COND_LINE_FORMAT.matcher(line);
            if (!m.matches()) {
                throw new IllegalArgumentException(
                  "invalid rewrite rule condition : " + line
                );
            }
            invertMatch = (m.group(1) != null);
            paramType   = (m.group(2) == null) ? "" : m.group(2);
            paramName   =  m.group(3);
            pattern     = Pattern.compile(m.group(4), Pattern.COMMENTS);
        }
        
        /**
         * この条件にマッチするかどうかを返す。
         * @param value    判定対象値
         * @param backRefs バックリファレンス用のホルダ
         * @return この条件にマッチしていればtrue
         */
        public boolean satisfiedBy(Object value, Map<String, List<String>> backRefs) {
            String val = (value == null) ? "" : StringUtil.toString(value);
            Matcher m = pattern.matcher(val);
            boolean found = m.find();
            if (found) {
                List<String> backRef = new ArrayList<String>();
                for (int i = 0; i <= m.groupCount(); i++) {
                    backRef.add(m.group(i));
                }
                backRefs.put(paramType + ":" + paramName, backRef);
            }
            return found ^ invertMatch;
        }
    }
    
    /** 変数定義 */
    private static final class Export {
        /** 変数種別 */
        private final String paramType;
        /** 変数名 */
        private final String paramName;
        /** パターン */
        private final String paramValue;
        /**
         * コンストラクタ。
         * @param line 定義文字列
         */
        private Export(String line) {
            Matcher m = COND_LINE_FORMAT.matcher(line);
            if (!m.matches() || (m.group(1) != null)) {
                throw new IllegalArgumentException(
                  "invalid rewrite rule condition : " + line
                );
            }
            paramType  = (m.group(2) == null) ? "" : m.group(2);
            paramName  =  m.group(3);
            paramValue =  m.group(4);
        }
    }
    /** 記述書式 */
    private static final Pattern COND_LINE_FORMAT = Pattern.compile(
      "^(!)?"                              // Capture#1 否定
    + "\\%\\{"                             // %{
    +   "(?:([-_a-zA-Z][-_a-zA-Z0-9]+):)?" // Capture#2 変数種別
    +   "([-_.a-zA-Z][-_.a-zA-Z0-9]+)"     // Capture#3 変数名
    + "\\}"                                // }
    + "\\s+"
    + "(.*)"                               // Capture#4 パターン
    );
    
    
    // --------------------------------------------------- properties
    /** 処理対象パターン */
    private Pattern pattern;
    
    /** 置換先文字列 */
    private String rewriteTo;
    
    /** 適用条件 */
    private final List<Condition> conditions = new ArrayList<Condition>();
    
    /** 変数定義 */
    private final List<Export> exports = new ArrayList<Export>();
    
    
    // ---------------------------------------------------- template methods
    /**
     * 書き換え対象のパスを取得する。
     * 
     * @param data 処理対象オブジェクト
     * @return 書き換え対象パス文字列
     */
    protected abstract String getPathToRewrite(TData data);
    
    /**
     * 書き換えられたパスを処理対象オブジェクトに反映する。
     * @param rewrittenPath 書き換えられたパス
     * @param data 処理対象オブジェクト
     */
    protected abstract void applyRewrittenPath(String rewrittenPath, TData data);
    
    /**
     * 変数の値を返す。
     * 
     * この実装では、以下の変数種別に対応する。
     * <pre>
     * ----------- ------------------------
     * 種別名       内容
     * ----------- ------------------------
     * request     リクエストスコープ変数
     * session     セッションスコープ変数
     * thread      スレッドコンテキスト変数
     * ----------- ------------------------
     * </pre>
     * なお、該当する変数が定義されていなかった場合はnullを返す。
     * 
     * @param scope   変数種別
     * @param name    変数名
     * @param data    処理対象オブジェクト
     * @param context 実行コンテキスト
     * @return 変数の値
     */
    protected Object
    getParam(String scope, String name, TData data, ExecutionContext context) {
        return "request".equals(scope) ? context.getRequestScopedVar(name)
             : "session".equals(scope) ? context.getSessionScopedVar(name)
             : "thread".equals(scope)  ? ThreadContext.getObject(name)
             : null;
    }
    
    /**
     * 変数を定義する。
     * 
     * @param scope   変数種別
     * @param name    変数名
     * @param value   変数の値
     * @param data    処理対象オブジェクト
     * @param context 実行コンテキスト
     */
    protected void
    exportParam(String scope, String name, String value, TData data, ExecutionContext context) {
        if ("request".equals(scope)) {
            context.setRequestScopedVar(name, value);
        } else if ("session".equals(scope)) {
            context.setSessionScopedVar(name, value);
        } else if ("thread".equals(scope)) {
            ThreadContext.setObject(name, value);
        }
    }
    
    
    // ------------------------------------------------------- main logic
    /**
     * このオブジェクトの設定に従ってパスの置換処理をおこない、
     * 置換後のパス文字列を返す。
     * 置換処理が行われなかった場合はnullを返す。
     * 
     * @param data    処理対象オブジェクト
     * @param context 実行コンテキスト
     * @return 置換処理が行われた場合は置換後の文字列。
     *          行われなかった場合はnull。
     */
    public String rewrite(TData data, ExecutionContext context) {
        String fromPath = getPathToRewrite(data);
        String toPath   = rewriteTo;
        
        Map<String, List<String>> backRefs = new HashMap<String, List<String>>();
        
        for (Condition cond : conditions) {
            Object value = getParam(cond.paramType, cond.paramName, data, context);
            if (!cond.satisfiedBy(value, backRefs)) {
                return null;
            }
        }
        
        Matcher m = pattern.matcher(fromPath);
        if (!m.matches()) {
            return null;
        }
        
        List<String> backRef = new ArrayList<String>(m.groupCount());
        for (int i = 0; i <= m.groupCount(); i++) {
            backRef.add(m.group(i));
        }
        backRefs.put("#", backRef);
        
        String rewrittenPath = (toPath == null)
                             ? fromPath
                             : interpolate(toPath, backRefs, data, context);
        
        applyRewrittenPath(rewrittenPath, data);

        for (Export export : exports) {
            exportParam(
                export.paramType
              , export.paramName
              , interpolate(export.paramValue, backRefs, data, context)
              , data
              , context
            );
        }
        return rewrittenPath;
    }
    
    
    // --------------------------------------------------------- helpers
    /**
     * 埋め込み文字列を反映する。
     * @param str      処理対象文字列
     * @param backRefs バックリファレンス
     * @param data     処理対象オブジェクト
     * @param context  実行コンテキスト
     * @return 処理結果文字列
     */
    private String interpolate(String                    str,
                               Map<String, List<String>> backRefs,
                               TData                     data,
                               ExecutionContext          context) {
        
        Matcher placeHolder = PLACE_HOLDER.matcher(str);
        String  result = str;
        
        while (placeHolder.find()) {
            Object value;
            if (placeHolder.group(1) != null) {
                value = backRefs.get("#")
                                .get(Integer.valueOf(placeHolder.group(1)));
            } else {
                String type = (placeHolder.group(2) == null) ? "" : placeHolder.group(2);
                String name = placeHolder.group(3);
                String backRefNum = placeHolder.group(4);
                               
                value = (backRefNum == null)
                      ? getParam(type, name, data, context)
                      : backRefs.get(type + ":" + name)
                                .get(Integer.valueOf(backRefNum));
            }
            result = result.replace(
                placeHolder.group()
              , (value == null) ? "" : StringUtil.toString(value)
            );
        }
        return result;
    }
    
    /** 埋め込み変数のプレースホルダー */
    private static final Pattern PLACE_HOLDER = Pattern.compile(
      "\\$\\{"
    + "(?:"
    +   "([1-9][0-9]*|0)"              // Capture#1 バックリファレンス(のみ)
    + "|"
    +   "(?:([a-z]+)\\:)?"             // Capture#2 変数種別
    +   "([-_.a-zA-Z][-_.a-zA-Z0-9]+)" // Capture#3 変数名
    +   "(?:\\:((?:[1-9][0-9]*|0)))?"  // Capture#4 バックリファレンス番号
    + ")"
    + "\\}"
    , Pattern.CASE_INSENSITIVE);
    
    
    // ----------------------------------------------------------- accessors
    /**
     * この置換ルールが適用されるパスのパターンを正規表現で設定する。
     * @param  pattern この置換ルールが適用されるパスのパターン
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf setPattern(String pattern) {
        if (StringUtil.isNullOrEmpty(pattern)) {
            throw new IllegalArgumentException(
                "The property [pattern] must not be null or blank."
            );
        }
        this.pattern = Pattern.compile(pattern.trim());
        return (TSelf) this;
    }
    
    /**
     * この置換ルールが適用された場合に置き換えられる文字列を指定する。
     * この文字列中では、以下の埋め込みパラメータを使用することができる。
     * 
     * @param  rewriteTo この置換ルールが適用された場合に置き換えられる文字列
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf setRewriteTo(String rewriteTo) {
        if (StringUtil.isNullOrEmpty(rewriteTo)) {
            throw new IllegalArgumentException(
                "The property [rewriteTo] must not be null or blank."
            );
        }
        this.rewriteTo = rewriteTo;
        return (TSelf) this;
    }
    
    /**
     * 変数定義を設定する。
     * 
     * 既存の設定はクリアされる。
     * 
     * @param exportDefinitions 変数定義
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf setExports(List<String> exportDefinitions) {
        exports.clear();
        for (String def : exportDefinitions) {
            addExport(def);
        }
        return (TSelf) this;
    }
    
    /**
     * リクエストスコープ変数定義を追加する。
     * 
     * 同名の変数が既に定義されていた場合は上書きする。
     * 
     * @param exportDefinition 変数名
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf addExport(String exportDefinition) {
        exports.add(new Export(exportDefinition));
        return (TSelf) this;
    }

    /**
     * 置換処理の適用条件を設定する。
     * 
     * 既存の設定はクリアされる。
     * 
     * @param conditions 適用条件
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf setConditions(List<String> conditions) {
        this.conditions.clear();
        for (String cond : conditions) {
            addCondition(cond);
        }
        return (TSelf) this;
    }
    
    /**
     * 置換処理の適用条件を追加する。
     * 
     * @param condition 適用条件
     * @return このオブジェクト自体
     */
    @SuppressWarnings("unchecked")
    public TSelf addCondition(String condition) {
        conditions.add(new Condition(condition));
        return (TSelf) this;
    }
}
