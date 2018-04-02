package io.dashbase.web.server;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import io.dashbase.KafkaSink;
import io.dashbase.avro.DashbaseEvent;
import io.dashbase.ter.xform.DashbaseEventHydrant;
import io.dashbase.ter.xform.FilebeatLineParser;
import io.dashbase.utils.PrometheusUtils;
import okhttp3.OkHttpClient;
import org.hawkular.agent.prometheus.PrometheusScraper;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class MetricsResource {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusResource.class);
    private final KafkaSink kafkaSink;
    private final DashbaseEventHydrant hydrant;
    private final static OkHttpClient client = new OkHttpClient();

    private final Meter countMeter;
    private final Meter bytesMeter;
    private final Meter hydrantErrorMeter;

    public MetricsResource(KafkaSink kafkaSink, String outputFormat, MetricRegistry metricRegistry) {
        this.kafkaSink = kafkaSink;

        if (outputFormat == null) {
            hydrant = null;
        } else {
            switch (outputFormat) {
                case "avro":
                    this.hydrant = new FilebeatLineParser();
                    break;
                case "raw":
                default:
                    this.hydrant = null;
            }
        }

        countMeter = metricRegistry.meter("dashbase.prometheus.events");
        bytesMeter = metricRegistry.meter("dashbase.prometheus.bytes");
        hydrantErrorMeter = metricRegistry.meter("dashbase.prometheus.hydrant.error");
    }

    @Path("metrics")
    @GET
    @Timed
    public Response metrics() throws Exception {
        PrometheusScraper prometheusScraper = new PrometheusScraper(new URL("http://localhost:9090/metrics"));
        List<MetricFamily> metricFamilies = prometheusScraper.scrape();

        List<Map<String, Object>> datas = PrometheusUtils.getDatas(metricFamilies);

        datas.forEach(v -> {
            try {
                kafkaSink.send("dashbase", utils.addInformation(v).binaryValue());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
        return Response.ok().entity(null).build();

    }

    private boolean insert(String index, String type,
                           String id, byte[] jsonPayload) throws Exception {
        byte[] data = jsonPayload;

        if (data != null) {
            if (hydrant != null) {
                DashbaseEvent event = hydrant.apply(data);
                if (event == null) {
                    hydrantErrorMeter.mark();
                    return false;
                }
                data = event.toByteBuffer().array();
            }
            kafkaSink.send(index, data);
            countMeter.mark();
            bytesMeter.mark(data.length);
        }
        return true;
    }

    private boolean insertBulk(String index, ServletInputStream in) throws Exception {
        long start = System.currentTimeMillis();
//        BulkIndexResult res = new BulkIndexResult();
        boolean error = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
        List<byte[]> bytesList = new LinkedList<>();
        long byteCount = 0L;
        String dataLine;
        while ((dataLine = reader.readLine()) != null) {
            byte[] bytes = dataLine.getBytes(Charsets.UTF_8);
            byteCount += bytes.length;

            if (hydrant != null) {
                DashbaseEvent event = hydrant.apply(bytes);
                if (event == null) {
                    hydrantErrorMeter.mark();
//                    res.errors = true;
                    error = true;
                    continue;
                }
                bytes = event.toByteBuffer().array();
            }

            bytesList.add(bytes);
        }
        kafkaSink.sendBulk(index, bytesList);
        bytesMeter.mark(byteCount);
        countMeter.mark(bytesList.size());
//        res.took = start - System.currentTimeMillis();
//        return res;
        return error;
    }

}

