package io.dashbase.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hawkular.agent.prometheus.types.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrometheusEventTest {
    @Test
    public void TestGetPromEventsFromMetricFamily() throws JsonProcessingException {
        Map<String, String> labels = new HashMap<String, String>() {{
            put("handler", "query");
        }};

        Map<String, Object> values = new HashMap<String, Object>() {{
            put("value", 10d);
        }};
        List<MetricFamily> metricFamilies = new ArrayList<MetricFamily>() {
            {
                add(new MetricFamily.Builder()
                        .setName("http_request_duration_microseconds")
                        .setHelp("foo")
                        .setType(MetricType.COUNTER)
                        .addMetric(
                                new Counter.Builder()
                                        .addLabels(labels)
                                        .setName("http_request_duration_microseconds")
                                        .setValue(10d)
                                        .build()
                        ).build());

                add(new MetricFamily.Builder()
                        .setName("http_request_duration_microseconds")
                        .setHelp("foo")
                        .setType(MetricType.GAUGE)
                        .addMetric(
                                new Gauge.Builder()
                                        .setName("http_request_duration_microseconds")
                                        .setValue(10d)
                                        .build()
                        ).build());

                add(new MetricFamily.Builder()
                        .setName("http_request_duration_microseconds")
                        .setHelp("foo")
                        .setType(MetricType.SUMMARY)
                        .addMetric(
                                new Summary.Builder()
                                        .setName("http_request_duration_microseconds")
                                        .setSampleCount(10)
                                        .setSampleSum(10d)
                                        .addQuantile(0.99d, 10d)
                                        .build()
                        ).build());

                add(new MetricFamily.Builder()
                        .setName("http_request_duration_microseconds")
                        .setHelp("foo")
                        .setType(MetricType.HISTOGRAM)
                        .addMetric(
                                new Histogram.Builder()
                                        .setName("http_request_duration_microseconds")
                                        .setSampleCount(10)
                                        .setSampleSum(10d)
                                        .addBucket(0.99d, 10)
                                        .build()
                        ).build());
            }
        };
        List<PrometheusEvent> prometheusEvents = new ArrayList<PrometheusEvent>() {{
            add(
                    new PrometheusEvent()
                            .setKey("http_request_duration_microseconds")
                            .setLabelHash(labels.hashCode())
                            .setLabels(labels)
                            .setValue(values)
            );
            add(
                    new PrometheusEvent()
                            .setKey("http_request_duration_microseconds")
                            .setLabelHash("#".hashCode())
                            .setValue(values)
            );
            add(
                    new PrometheusEvent()
                            .setKey("http_request_duration_microseconds")
                            .setLabelHash("#".hashCode())
                            .setValue(
                                    new HashMap<String, Object>() {{
                                        put("count", 10);
                                        put("sum", 10d);
                                        put("percentile", new HashMap<String, Object>() {{
                                            put("99", 10d);
                                        }});
                                    }}
                            )
            );
            add(
                    new PrometheusEvent()
                            .setKey("http_request_duration_microseconds")
                            .setLabelHash("#".hashCode())
                            .setValue(new HashMap<String, Object>() {{
                                put("count", 10);
                                put("sum", 10d);
                                put("bucket", new HashMap<String, Object>() {{
                                    put("0.99", 10);
                                }});
                            }})
            );
        }};

        for (int i = 0; i < metricFamilies.size(); i++) {
            List<PrometheusEvent> prometheusEventList = PrometheusUtils.toPromEvents(metricFamilies.get(i));
            Assertions.assertEquals(1, prometheusEventList.size());
            Assertions.assertEquals(new ObjectMapper().writeValueAsString(prometheusEventList.get(0)),
                    new ObjectMapper().writeValueAsString(prometheusEvents.get(i)));
        }
    }


}



