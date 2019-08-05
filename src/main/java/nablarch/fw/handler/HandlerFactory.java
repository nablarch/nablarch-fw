package nablarch.fw.handler;

public interface HandlerFactory {

    Object create(Class<?> clazz) throws InstantiationException, IllegalAccessException;
}