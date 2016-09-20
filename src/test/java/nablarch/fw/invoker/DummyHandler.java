package nablarch.fw.invoker;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 */
class DummyHandler implements Handler<Object, Object> {

    @Override
    public Object handle(Object o, ExecutionContext context) {
        return null;
    }
}
