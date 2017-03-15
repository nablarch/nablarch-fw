package nablarch.fw.handler.dispatch.test3.ss00A001;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;

public class W11AC002Action implements Handler<Request<String>, String> {
    
    public String handle(Request<String> data, ExecutionContext context) {
        context.setRequestScopedVar("executeAction", "test3.W11AC002Action");
        return "test3.W11AC002Action";
    }

}

