package io.dashbase.web.server;

import io.dashbase.PrometheusProxyApplication;
import io.dashbase.client.metrics.DashbaseMetrics;
import io.dashbase.web.converter.ResponseFactory;
import io.dashbase.web.response.Response;
import org.apache.kafka.common.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapid.api.NumericAggregationRequest;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;
import rapid.api.TimeRangeFilter;
import rapid.api.query.EqualityQuery;
import rapid.api.query.StringQuery;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

import static io.dashbase.PrometheusProxyApplication.httpService;

@Path("/api/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class PrometheusResource {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusResource.class);


    //    {
    //        "numResults":0,
    //            "tableNames":[
    //        "_metrics"
    //    ],
    //        "excludeTableNames":[
    //
    //    ],
    //        "query":{
    //        "queryType":"equality",
    //                "col":"service",
    //                "value":"api",
    //                "equal":true
    //    },
    //        "timeRangeFilter":{
    //        "startTimeInSec":1522670146,
    //                "endTimeInSec":1522670206
    //    },
    //        "fields":[
    //
    //    ],
    //        "aggregations":{
    //        "cpu_usage":{
    //            "requestType":"numeric",
    //                    "col":"jvm.cpu.usage.percent.value",
    //                    "type":"avg"
    //        }
    //    },
    //        "useApproximation":false,
    //            "ctx":"8a4fa7e4dc86b085",
    //            "fetchSchema":false,
    //            "timeoutMillis":2147483647,
    //            "disableHighlight":true,
    //            "debugMode":0
    //    }
    @Path("query")
    @GET
    public Response query(
            @QueryParam("query") String query,
            @QueryParam("time") String time,
            @QueryParam("timeout") long timeout
    ) throws Exception {
        //        long timeMillis = time == null ? System.currentTimeMillis() : DateUtils.timeNum(time);
        //        ResponseFactory factory = ResponseFactory.of(query, timeMillis);
        //        RapidRequest request = factory.createRequest();
        //        return factory.instantQuery();

        ResponseFactory factory = ResponseFactory.of(query, 1522670199);
        RapidResponse response = httpService.query(factory.createRequest());
        System.out.println(response);
        return null;
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
            @QueryParam("step") int step,
            @QueryParam("timeout") long timeout
    ) {
        return null;
    }
}
