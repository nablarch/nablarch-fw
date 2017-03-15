package nablarch.fw.handler.dispatch.base.ss00A002;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;

public class W11AC001Action implements Handler<Request<String>, String> {
    
    public String handle(Request<String> data, ExecutionContext context) {
        context.setRequestScopedVar("executeAction", "base.W11AC001Action");
        return "base.W11AC001Action";
    }

}
