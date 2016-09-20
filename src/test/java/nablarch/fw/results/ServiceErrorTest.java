package nablarch.fw.results;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.cache.BasicStaticDataCache;
import nablarch.core.log.basic.LogLevel;
import nablarch.core.message.PropertiesStringResourceLoader;
import nablarch.core.message.StringResource;
import nablarch.core.message.StringResourceHolder;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ServiceError}のテストクラス。
 */
public class ServiceErrorTest {

    @Before
    public void setUp() throws Exception {
        ThreadContext.clear();
        SystemRepository.clear();

        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> result = new HashMap<String, Object>();

                final PropertiesStringResourceLoader resourceLoader = new PropertiesStringResourceLoader();
                resourceLoader.setLocales(Arrays.asList("en"));
                resourceLoader.setDefaultLocale("ja");

                final BasicStaticDataCache<StringResource> cache = new BasicStaticDataCache<StringResource>();
                cache.setLoader(resourceLoader);
                cache.initialize();

                final StringResourceHolder stringResourceHolder = new StringResourceHolder();
                stringResourceHolder.setStringResourceCache(cache);

                result.put("stringResourceHolder", stringResourceHolder);
                return result;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        ThreadContext.clear();
        SystemRepository.clear();
    }

    /**
     * メッセージIDとスレッドコンテキストの言語情報を元にメッセージが取得できること。
     */
    @Test
    public void getMessageFromMessageId_dependThreadContext() throws Exception {
        ThreadContext.setLanguage(Locale.ENGLISH);

        final ServiceError sut = new ServiceError(LogLevel.FATAL, "service.error.text.message") {
        };
        final String message = sut.getMessage();
        assertThat(message, is("service error test"));
    }

    /**
     * スレッドコンテキスト上に言語情報が存在しない場合、デフォルトのロケールを元にメッセージが構築されること。
     *
     * @throws Exception
     */
    @Test
    public void getMessageFromMessageId_notDependThreadContext() throws Exception {
        final ServiceError sut = new ServiceError(LogLevel.FATAL, "service.error.text.message") {
        };
        final String message = sut.getMessage();
        assertThat(message, is("サービスエラーテスト"));
    }
}
