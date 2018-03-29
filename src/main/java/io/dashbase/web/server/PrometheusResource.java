package io.dashbase.web.server;

import com.google.common.collect.Sets;
import io.dashbase.eval.Evaluator;
import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.InstantQuery;
import io.dashbase.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;
import rapid.api.query.Query;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static io.dashbase.PrometheusProxyApplication.httpService;

@Path("/api/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class PrometheusResource {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusResource.class);

    @Path("query")
    @GET
    public Response<BaseResult<InstantQuery>> query(
            @QueryParam("query") String query,
            @QueryParam("time") String time,
            @QueryParam("timeout") String timeout
    ) throws Exception {
        Query rapidQuery = Evaluator.eval(query);
        RapidRequest rapidRequest = new RapidRequest();
        rapidRequest.tableNames = Sets.newHashSet("nginx_json");
        rapidRequest.query = rapidQuery;
        RapidResponse rapidRes = httpService.query(rapidRequest);
        System.out.println(rapidRes);
        return null;
    }
}
