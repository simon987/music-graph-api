package net.simon987.musicgraph.webapi;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.grizzly.utils.Exceptions;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {

        //TODO: logger

        return Response.status(500)
                .entity(Exceptions.getStackTraceAsString(ex))
                .type("text/plain")
                .build();
    }
}

