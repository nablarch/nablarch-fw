package nablarch.fw;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.message.MessageNotFoundException;
import nablarch.core.message.StringResource;
import nablarch.core.message.StringResourceHolder;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.fw.results.BadRequest;
import nablarch.fw.results.Conflicted;
import nablarch.fw.results.Forbidden;
import nablarch.fw.results.RequestEntityTooLarge;
import nablarch.fw.results.ServiceError;
import nablarch.fw.results.ServiceUnavailable;
import nablarch.fw.results.Unauthorized;
import nablarch.test.support.log.app.OnMemoryLogWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResultTest {

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("stringResourceHolder", new StringResourceHolder() {
                    @Override
                    public StringResource get(final String messageId) throws MessageNotFoundException {
                        return new StringResource() {
                            @Override
                            public String getId() {
                                return messageId;
                            }

                            @Override
                            public String getValue(Locale locale) {
                                if (messageId.equals("notfound")) {
                                    throw new IllegalArgumentException();
                                }
                                return "error message";
                            }
                        };
                    }
                });
                return result;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        SystemRepository.clear();
    }

    @Test
    public void testSuccess() {
        Result.Success success = new Result.Success();
        assertThat(success.getStatusCode(), is(200));
        assertThat(success.getMessage(), is("The request has succeeded."));
        assertThat(success.isSuccess(), is(true));
        assertThat(success.toString(), is(containsString("200")));
        assertThat(success.toString(), is(containsString("Success")));
        assertThat(success.toString(), is(containsString("The request has succeeded.")));

        Result.Success ok = new Result.Success("OK!!");
        assertThat(ok.getMessage(), is("OK!!"));
        assertThat(ok.getStatusCode(), is(200));
        assertThat(success.isSuccess(), is(true));
    }

    @Test
    public void testMultiStatus() {
        Result.MultiStatus multiStatus = new Result.MultiStatus();
        assertThat(multiStatus.getStatusCode(), is(207));
        assertThat(multiStatus.getMessage(), is("The result of your request has multiple status. "
                + "Please consult following messages."));
        assertThat(multiStatus.isSuccess(), is(true));

        multiStatus.addResults(new Result.Success("OK"));
        assertThat(multiStatus.getResults().size(), is(1));
        assertThat(multiStatus.isSuccess(), is(true));

        multiStatus.addResults(new nablarch.fw.results.InternalError("NG"));
        assertThat(multiStatus.getResults().size(), is(2));
        assertThat(multiStatus.isSuccess(), is(false));

        Result.MultiStatus message = new Result.MultiStatus("multi");
        assertThat(message.getMessage(), is("multi"));

    }

    @Test
    public void testError() {
        class ErrorSub extends Result.Error {
            public ErrorSub(String message, Throwable cause) {
                super(message, cause);
            }
            public ErrorSub(Throwable cause) {
                super(cause);
            }
            public ErrorSub(String message) {
                super(message);
            }
            public ErrorSub() {
                super();
            }
        }

        ErrorSub error = new ErrorSub();
        assertThat(error.getStatusCode(), is(500));
        assertThat(error.getMessage(), is(nullValue()));
        assertThat(error.isSuccess(), is(false));
        assertThat(error.toString(), is(containsString("500")));
        assertThat(error.toString(), is(containsString("ErrorSub")));
        assertThat(error.getCause(), is(nullValue()));

        error = new ErrorSub("err");
        assertThat(error.getMessage(), is("err"));
        assertThat(error.getCause(), is(nullValue()));

        error = new ErrorSub(new NullPointerException());
        assertThat(error.getMessage(), is("java.lang.NullPointerException"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new ErrorSub("err", new NullPointerException());
        assertThat(error.getMessage(), is("err"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));
    }

    @Test
    public void testClientError() {
        class ClientErrorSub extends Result.ClientError {
            public ClientErrorSub() {
                super();
            }
            public ClientErrorSub(String message) {
                super(message);
            }
            public ClientErrorSub(Throwable cause) {
                super(cause);
            }
            public ClientErrorSub(String message, Throwable cause) {
                super(message, cause);
            }
        }

        ClientErrorSub clientError = new ClientErrorSub();
        assertThat(clientError.getStatusCode(), is(400));
        assertThat(clientError.getMessage(), is(nullValue()));
        assertThat(clientError.getCause(), is(nullValue()));

        clientError = new ClientErrorSub("error");
        assertThat(clientError.getMessage(), is("error"));
        assertThat(clientError.getCause(), is(nullValue()));

        clientError = new ClientErrorSub(new NullPointerException("null"));
        assertThat(clientError.getMessage(), is(containsString("null")));
        assertThat(clientError.getCause(), is(instanceOf(NullPointerException.class)));

        clientError = new ClientErrorSub("error", new NullPointerException("null"));
        assertThat(clientError.getMessage(), is("error"));
        assertThat(clientError.getCause(), is(instanceOf(NullPointerException.class)));
    }

    @Test
    public void testBadRequest() {
        BadRequest badRequest = new BadRequest();
        assertThat(badRequest.getStatusCode(), is(400));
        assertThat(badRequest.getMessage(), is("The request could not be processed "
                + "due to malformed syntax or not being consistent. "
                + "Please check your request."));

        BadRequest bad = new BadRequest("bad");
        assertThat(bad.getMessage(), is("bad"));

        bad = new BadRequest("bad", new NullPointerException());
        assertThat(bad.getMessage(), is("bad"));
        assertThat(bad.getCause(), is(instanceOf(NullPointerException.class)));

        bad = new BadRequest(new NullPointerException());
        assertThat(bad.getMessage(), is("java.lang.NullPointerException"));
        assertThat(bad.getCause(), is(instanceOf(NullPointerException.class)));
    }

    @Test
    public void testUnauthorizedError() {
        String message = "you aren't unauthorized!";
        Unauthorized error = new Unauthorized(message);
        assertEquals(401, error.getStatusCode());
        assertEquals(message, error.getMessage());

        error = new Unauthorized();
        assertThat(error.getMessage(), is("The request you send requires authentication."));

        error = new Unauthorized(new NullPointerException());
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new Unauthorized("error", new NullPointerException());
        assertThat(error.getMessage(), is("error"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

    }

    @Test
    public void testForbidden() {
        Forbidden forbidden = new Forbidden();
        assertThat(forbidden.getStatusCode(), is(403));
        assertThat(forbidden.getMessage(), is("The request you send was refused "
                + "due to insufficient authorization."));

        forbidden = new Forbidden("forbidden");
        assertThat(forbidden.getMessage(), is("forbidden"));
        assertThat(forbidden.getCause(), is(nullValue()));

        forbidden = new Forbidden(new IllegalArgumentException());
        assertThat(forbidden.getMessage(), is("java.lang.IllegalArgumentException"));
        assertThat(forbidden.getCause(), is(instanceOf(IllegalArgumentException.class)));

        forbidden = new Forbidden("error", new IllegalArgumentException());
        assertThat(forbidden.getMessage(), is("error"));
        assertThat(forbidden.getCause(), is(instanceOf(IllegalArgumentException.class)));


    }
    
    @Test
    public void testNotFound() {
        Result.NotFound error = new Result.NotFound();
        assertThat(error.getStatusCode(), is(404));
        assertThat(error.getMessage(), is("There was no resources matching your request."));
        assertThat(error.getCause(), is(nullValue()));

        String message = "There are no resources you are looking for!";
        error = new Result.NotFound(message);
        assertEquals(404, error.getStatusCode());
        assertEquals(message, error.getMessage());

        error = new Result.NotFound(new NullPointerException());
        assertThat(error.getMessage(), is("java.lang.NullPointerException"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new Result.NotFound("not found.", new NullPointerException());
        assertThat(error.getMessage(), is("not found."));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));
    }

    @Test
    public void testConflicted() {
        Conflicted conflicted = new Conflicted();
        assertThat(conflicted.getStatusCode(), is(409));
        assertThat(conflicted.getMessage(), is("The request could not be processed "
                + "due to a conflict with the current status of "
                + "the resource you requested or a concurrent request "
                + "accessing the same resource."));
        assertThat(conflicted.getCause(), is(nullValue()));

        conflicted = new Conflicted("conflicted");
        assertThat(conflicted.getMessage(), is("conflicted"));
        assertThat(conflicted.getCause(), is(nullValue()));

        conflicted = new Conflicted(new NullPointerException());
        assertThat(conflicted.getMessage(), is("java.lang.NullPointerException"));
        assertThat(conflicted.getCause(), is(instanceOf(NullPointerException.class)));

        conflicted = new Conflicted("err", new NullPointerException());
        assertThat(conflicted.getMessage(), is("err"));
        assertThat(conflicted.getCause(), is(instanceOf(NullPointerException.class)));
    }

    @Test
    public void testRequestEntityTooLarge() {
        RequestEntityTooLarge large = new RequestEntityTooLarge();
        assertThat(large.getStatusCode(), is(413));
        assertThat(large.getMessage(), is("The request you send was refused "
                + "because the request entity is larger than "
                + "the server is willing or able to process."));
        assertThat(large.getCause(), is(nullValue()));

        large = new RequestEntityTooLarge("large");
        assertThat(large.getMessage(), is("large"));
        assertThat(large.getCause(), is(nullValue()));

        large = new RequestEntityTooLarge(new IllegalStateException());
        assertThat(large.getMessage(), is("java.lang.IllegalStateException"));
        assertThat(large.getCause(), is(instanceOf(IllegalStateException.class)));

        large = new RequestEntityTooLarge("error", new IllegalStateException());
        assertThat(large.getMessage(), is("error"));
        assertThat(large.getCause(), is(instanceOf(IllegalStateException.class)));
    }

    @Test
    public void testServiceError() {
        class ServiceErrorSub extends ServiceError {
            public ServiceErrorSub() {
                super();
            }
            public ServiceErrorSub(String message) {
                super(message);
            }
            public ServiceErrorSub(Throwable cause) {
                super(cause);
            }
            public ServiceErrorSub(String message, Throwable cause) {
                super(message, cause);
            }
            public ServiceErrorSub(LogLevel logLevel, String messageId, Object... messageParams) {
                super(logLevel, messageId, messageParams);
            }
            public ServiceErrorSub(LogLevel logLevel, Throwable cause, String messageId, Object... messageParams) {
                super(logLevel, cause, messageId, messageParams);
            }
        }

        ServiceErrorSub serviceError = new ServiceErrorSub();
        assertThat(serviceError.getStatusCode(), is(500));
        assertThat(serviceError.getMessage(), is(nullValue()));
        assertThat(serviceError.getCause(), is(nullValue()));
        assertThat(serviceError.isSuccess(), is(false));
        assertThat(serviceError.getMessageParams(), is(new Object[0]));
        assertThat(serviceError.getMessageId(), is(nullValue()));

        OnMemoryLogWriter.clear();
        serviceError.writeLog(null);
        List<String> log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub("message");
        assertThat(serviceError.getMessage(), is("message"));
        serviceError.writeLog(null);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));
        assertThat(log.get(0), is(containsString("message")));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(new NullPointerException());
        assertThat(serviceError.getMessage(), is("java.lang.NullPointerException"));
        assertThat(serviceError.getCause(), is(instanceOf(NullPointerException.class)));
        serviceError.writeLog(null);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));
        assertThat(log.get(0), is(containsString("java.lang.NullPointerException")));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub("message", new NullPointerException());
        assertThat(serviceError.getMessage(), is("message"));
        assertThat(serviceError.getCause(), is(instanceOf(NullPointerException.class)));
        serviceError.writeLog(null);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));
        assertThat(log.get(0), is(containsString("message")));

        ExecutionContext context = new ExecutionContext();
        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(LogLevel.FATAL, "messageId");
        assertThat(serviceError.getMessage(), is("error message"));
        serviceError.writeLog(context);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("FATAL")));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));
        assertThat(log.get(0), is(containsString("fail_code = [messageId]")));
        assertThat(log.get(0), is(containsString("error message")));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(LogLevel.ERROR, "messageId2");
        assertThat(serviceError.getMessage(), is("error message"));
        serviceError.writeLog(context);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("ERROR")));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));
        assertThat(log.get(0), is(containsString("fail_code = [messageId2]")));
        assertThat(log.get(0), is(containsString("error message")));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(LogLevel.WARN, "messageId3");
        assertThat(serviceError.getMessage(), is("error message"));
        serviceError.writeLog(context);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(0));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(LogLevel.DEBUG, "messageId3");
        assertThat(serviceError.getMessage(), is("error message"));
        serviceError.writeLog(context);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(0));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(LogLevel.FATAL, new NullPointerException(), "messageId");
        assertThat(serviceError.getMessage(), is("error message"));
        assertThat(serviceError.getCause(), is(instanceOf(NullPointerException.class)));
        final ServiceErrorSub finalServiceError = serviceError;
        context.setRequestScopedVar("nablarch_dataProcessedWhenThrownMap", new HashMap<Throwable, Object>() {{
            put(finalServiceError, "fugahoge");
        }});
        serviceError.writeLog(context);
        log = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(log.size(), is(1));
        assertThat(log.get(0), is(containsString("FATAL")));
        assertThat(log.get(0), is(containsString("500 ServiceErrorSub")));
        assertThat(log.get(0), is(containsString("fail_code = [messageId]")));
        assertThat(log.get(0), is(containsString("error message")));

        OnMemoryLogWriter.clear();
        serviceError = new ServiceErrorSub(LogLevel.FATAL, new NullPointerException(), "notfound");
        assertThat(serviceError.getMessage(), is("An error happened with messageId = [notfound](but couldn't get the message contents.)"));
        assertThat(serviceError.getCause(), is(instanceOf(NullPointerException.class)));
    }
    
    @Test
    public void testInternalError() {
        nablarch.fw.results.InternalError error = new nablarch.fw.results.InternalError();
        assertThat(error.getStatusCode(), is(500));
        assertThat(error.getMessage(), is("The request could not be processed "
                + "due to a unexpected condition. "
                + "please contact our support team if you need."));
        assertThat(error.getCause(), is(nullValue()));
        
        String message = "We can not process your request due to an internal error.";
        error = new nablarch.fw.results.InternalError(message);
        assertEquals(500, error.getStatusCode());
        assertEquals(message, error.getMessage());

        error = new nablarch.fw.results.InternalError("error", new NullPointerException());
        assertThat(error.getMessage(), is("error"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new nablarch.fw.results.InternalError(new NullPointerException());
        assertThat(error.getMessage(), is("java.lang.NullPointerException"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new nablarch.fw.results.InternalError(LogLevel.FATAL, "id");
        assertThat(error.getMessage(), is("error message"));
        assertThat(error.getCause(), is(nullValue()));

        error = new nablarch.fw.results.InternalError(LogLevel.FATAL, new NullPointerException(), "id");
        assertThat(error.getMessage(), is("error message"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));
    }
    
    @Test
    public void testServiceUnavailableError() {
        ServiceUnavailable error = new ServiceUnavailable();
        assertThat(error.getMessage(), is("This service is currently unable to handle your request "
                + "due to a temporary overloading or maintenance. "));
        assertThat(error.getStatusCode(), is(503));
        assertThat(error.getMessageId(), is(nullValue()));
        assertThat(error.getCause(), is(nullValue()));

        String message = "We can not process your request temporarily. please wait.";
        
        error = new ServiceUnavailable(message);
        Assert.assertNull(error.getRetryAfter());
        
        Date retryAfter = new GregorianCalendar().getTime();
        error.setRetryAfter(retryAfter);
        
        assertEquals(503, error.getStatusCode());
        assertEquals(retryAfter, error.getRetryAfter());
        assertEquals(message, error.getMessage());
        
        Assert.assertNull(error.setRetryAfter(null).getRetryAfter());

        error = new ServiceUnavailable("error", new NullPointerException());
        assertThat(error.getMessage(), is("error"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new ServiceUnavailable(new NullPointerException());
        assertThat(error.getMessage(), is("java.lang.NullPointerException"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));

        error = new ServiceUnavailable(LogLevel.FATAL, "id");
        assertThat(error.getMessage(), is("error message"));
        assertThat(error.getCause(), is(nullValue()));

        error = new ServiceUnavailable(LogLevel.FATAL, new NullPointerException(), "id");
        assertThat(error.getMessage(), is("error message"));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));
    }
}
