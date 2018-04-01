package io.dashbase.web.server;

import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.InstantQuery;
import io.dashbase.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class PrometheusResource {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusResource.class);

    @Path("query")
    @GET
    public Response<BaseResult<InstantQuery>> query() {
        throw new UnsupportedOperationException();
    }

}

