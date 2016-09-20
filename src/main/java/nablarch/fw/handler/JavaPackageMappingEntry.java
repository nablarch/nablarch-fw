package nablarch.fw.handler;

import nablarch.fw.RequestPathMatchingHelper;


/**
 * リクエストパスのパターン文字列とマッピング先Javaパッケージの関連を保持するクラス。
 * 
 * @author Masato Inoue
 */
public class JavaPackageMappingEntry {

    /** マッピング先Javaパッケージを設定する */
    private String basePackage;

    /** リクエストパスとリクエストパスのパターンの照合を行うクラス */
    private RequestPathMatchingHelper helper;
    
    /**
     * リクエストパスのパターン文字列を設定する。
     * @param requestPattern リクエストパスのパターン文字列
     * @return このオブジェクト自体
     */
    public JavaPackageMappingEntry setRequestPattern(String requestPattern) {
        helper = new RequestPathMatchingHelper(true).setRequestPattern(requestPattern);
        return this;
    }
    
    /**
     * リクエストパスとリクエストパスのパターンの照合を行うクラスを取得する。
     * @return リクエストパスとリクエストパスのパターンの照合を行うクラス
     */
    public RequestPathMatchingHelper getRequestPathMatching() {
        return helper;
    }
    
    /**
     * マッピング先Javaパッケージを取得する。
     * @return マッピング先Javaパッケージ
     */
    public String getBasePackage() {
        return basePackage;
    }
    
    /**
     * マッピング先Javaパッケージを設定する。
     * @param basePackage マッピング先Javaパッケージ
     * @return このオブジェクト自体
     */
    public JavaPackageMappingEntry setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
    
}
