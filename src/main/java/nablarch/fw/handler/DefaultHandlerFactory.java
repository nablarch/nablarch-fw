package nablarch.fw.handler;

public class DefaultHandlerFactory implements HandlerFactory {

    @Override
    public Object create(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }
}