package nablarch.fw.handler.dispatch.test4.ss00A001;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;

public class W11AC001Action implements Handler<Request<String>, String> {

    public String handle(Request<String> data, ExecutionContext context) {
        context.setRequestScopedVar("executeAction", "test4.W11AC001Action");
        return "test4.W11AC001Action";
    }

}

