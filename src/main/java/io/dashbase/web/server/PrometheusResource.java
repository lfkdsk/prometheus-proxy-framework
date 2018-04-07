package io.dashbase.web.server;

import com.google.common.collect.Sets;
import io.dashbase.eval.Evaluator;
import io.dashbase.utils.TypeUtils;
import io.dashbase.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapid.api.RapidServiceInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static io.dashbase.PrometheusProxyApplication.httpService;
import static io.dashbase.utils.DateUtils.timeMillis;
import static io.dashbase.web.response.Response.ErrorType.errorBadData;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

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
        long timeMillis = timeMillis(time);
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
        long startTimeMillis = timeMillis(start);
        long endTimeMillis = timeMillis(end);
        Duration interval = TypeUtils.parseDurationOrSecond(step);
        Evaluator evaluator = Evaluator.of(query, startTimeMillis, endTimeMillis, interval);
        return evaluator.runRangeQuery();
    }

    private static Pattern labelNamePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    @Path("label/{label_name}/values")
    @GET
    public Response values(
            @PathParam("label_name") String labelName
    ) throws Exception {
        if (!labelNamePattern.matcher(labelName).matches()) {
            return Response.error(errorBadData, format("invalid label name: %s", labelName));
        }

        RapidServiceInfo info = httpService.getInfo(Sets.newHashSet("_metrics"));
        List<String> labels = info.schema.fields.stream()
                                                .filter(field -> field.isNumeric)
//                                                .filter(field -> field.name.contains(labelName))
//                                                .filter(field -> field.name.startsWith("jvm"))
                                                .map(field -> field.name)
                                                .collect(toList());

        return Response.of(labels);
    }
}
