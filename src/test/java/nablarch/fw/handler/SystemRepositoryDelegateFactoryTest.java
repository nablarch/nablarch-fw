package nablarch.fw.handler;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.test.DummyComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class SystemRepositoryDelegateFactoryTest {
    SystemRepositoryDelegateFactory sut = new SystemRepositoryDelegateFactory();

    @Test
    public void testNormal() {
        // テスト用のリポジトリ構築
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/fw/handler/system-repository-delegate-factory-test.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);
        Object component = sut.create(DummyComponent.class);
        assertEquals(DummyComponent.class, component.getClass());
        assertEquals("dummy", ((DummyComponent) component).getValue());
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAbnormal() {
        expectedException.expect(IllegalConfigurationException.class);
        expectedException.expectMessage("specified nablarch.fw.handler.SystemRepositoryDelegateFactory is not registered in SystemRepository.");

        // テスト用のリポジトリ構築
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/fw/handler/system-repository-delegate-factory-test.xml");
        DiContainer container = new DiContainer(loader);
        SystemRepository.load(container);
        sut.create(SystemRepositoryDelegateFactory.class);
    }
}