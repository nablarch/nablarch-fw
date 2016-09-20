package nablarch.fw.invoker;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 */
public class StringifyHandler implements Handler<Object, String> {
    @Override
    public String handle(Object o, ExecutionContext context) {
        return String.valueOf(o);
    }
}
