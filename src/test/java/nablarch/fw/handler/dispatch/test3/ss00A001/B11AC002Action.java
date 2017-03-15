package nablarch.fw.handler.dispatch.test3.ss00A001;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;
import nablarch.fw.Result;
import nablarch.fw.Result.Success;

public class B11AC002Action implements Handler<Request<String>, Result> {
    
    public Result handle(Request<String> data, ExecutionContext context) {
        context.setRequestScopedVar("executeAction", "test3.B11AC002Action");
        return new Success();
    }

}
