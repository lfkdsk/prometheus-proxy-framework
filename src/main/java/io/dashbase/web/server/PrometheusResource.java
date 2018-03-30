package io.dashbase.web.server;

import io.dashbase.utils.DateUtils;
import io.dashbase.web.converter.ResponseFactory;
import io.dashbase.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapid.api.RapidRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class PrometheusResource {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusResource.class);

    @Path("query")
    @GET
    public Response query(
            @QueryParam("query") String query,
            @QueryParam("time") String time,
            @QueryParam("timeout") long timeout
    ) throws Exception {
        long timeMillis = time == null ? System.currentTimeMillis() : DateUtils.timeNum(time);
        ResponseFactory factory = ResponseFactory.of(query, timeMillis);
        RapidRequest request = factory.createRequest();
        return factory.instantQuery();
    }
}
