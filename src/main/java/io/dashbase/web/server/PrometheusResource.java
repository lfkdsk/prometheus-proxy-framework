package io.dashbase.web.server;

import io.dashbase.eval.Evaluator;
import io.dashbase.utils.DateUtils;
import io.dashbase.utils.TypeUtils;
import io.dashbase.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.Objects;

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
    ) {
        long timeMillis = Objects.isNull(time) ? System.currentTimeMillis() : DateUtils.timeNum(time);
        Evaluator evaluator = Evaluator.of(query, timeMillis);
        return evaluator.runInstantQuery();
    }

    /**
     * query=<string>: Prometheus expression query string.
     * start=<rfc3339 | unix_timestamp>: Start timestamp.
     * end=<rfc3339 | unix_timestamp>: End timestamp.
     * step=<duration>: Query resolution step width.
     * timeout=<duration>: Evaluation timeout. Optional. Defaults to and is capped by the value of the -query.timeout flag.
     */
    @Path("query_range")
    @GET
    public Response queryRange(
            @QueryParam("query") String query,
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("step") String step,
            @QueryParam("timeout") long timeout
    ) {
        long startTimeMillis = Objects.isNull(start) ? System.currentTimeMillis() : DateUtils.timeNum(start);
        long endTimeMillis = Objects.isNull(end) ? System.currentTimeMillis() : DateUtils.timeNum(end);
        Duration interval = TypeUtils.parseDuration(step);
        Evaluator evaluator = Evaluator.of(query, startTimeMillis, endTimeMillis, interval);
        return evaluator.runRangeQuery();
    }
}
