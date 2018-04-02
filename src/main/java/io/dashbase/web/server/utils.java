package io.dashbase.web.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hawkular.agent.prometheus.types.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class utils {



    public static List<PromEvent> toPromEvents(MetricFamily metricFamily) {
        String name = metricFamily.getName();
        List<PromEvent> promEvents = new ArrayList<>();
        for (Metric metric : metricFamily.getMetrics()) {
            PromEvent promEvent = new PromEvent();
            promEvent.setKey(name);
            promEvent.setLabelHash("#".hashCode());

            Map<String, Object> value = new HashMap<>();

            Map<String, String> labels = metric.getLabels();

            if (!labels.isEmpty()) {
                Map<String, String> tagsMap = new HashMap<>();

                labels.forEach((k, v) -> {
                    if (!k.isEmpty() && !v.isEmpty()) {
                        tagsMap.put(k, v);
                    }
                });
                promEvent.labels = tagsMap;
                promEvent.labelHash = tagsMap.hashCode();
            }

            switch (metricFamily.getType()) {
                case SUMMARY:
                    Summary summary = (Summary) metric;
                    value.put("sum", summary.getSampleSum());
                    value.put("count", summary.getSampleCount());
                    List<Summary.Quantile> quantiles = summary.getQuantiles();

                    Map<String, Double> percentileMap = new HashMap<>();
                    for (Summary.Quantile quantile : quantiles) {
                        String key = trimZeroOfNumber(100 * quantile.getQuantile());
                        if (!Double.isNaN(quantile.getValue())) {
                            percentileMap.put(key, quantile.getValue());
                        }
                    }
                    if (!percentileMap.isEmpty()) {
                        value.put("percentile", percentileMap);
                    }
                    break;
                case GAUGE:
                    Gauge gauge = (Gauge) metric;
                    value.put("value", gauge.getValue());
                    break;
                case COUNTER:
                    Counter counter = (Counter) metric;
                    value.put("value", counter.getValue());
                    break;
                case HISTOGRAM:
                    Histogram histogram = (Histogram) metric;
                    value.put("sum", histogram.getSampleSum());
                    value.put("count", histogram.getSampleCount());
                    Map<Double, Long> bucketMap = new HashMap<>();
                    for (Histogram.Bucket bucket : histogram.getBuckets()) {
                        bucketMap.put(bucket.getUpperBound(), bucket.getCumulativeCount());
                    }
                    if (!bucketMap.isEmpty()) {
                        value.put("bucket", bucketMap);
                    }
                    break;
            }

            promEvent.value = value;

            promEvents.add(promEvent);

        }
        return promEvents;
    }

    public static class Data {
        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> labels;
        @Getter
        @Setter
        @JsonProperty()
        Map<String, Object> value;
        @Setter
        @Getter
        @JsonProperty
        String namespace = "dashbase";
    }

    public static List<Map<String, Object>> getDatas(List<MetricFamily> metricFamilies) {
        Map<Integer, Data> eventList = new HashMap<>();
        for (MetricFamily metricFamily : metricFamilies) {
            for (PromEvent promEvent : toPromEvents(metricFamily)) {
                Data data = new Data();

                if (!promEvent.labels.isEmpty()) {
                    if (!eventList.containsKey(promEvent.labelHash)) {
                        data.labels = promEvent.labels;
                    }
                }

                data.value = new HashMap<String, Object>() {{
                    put(promEvent.key, promEvent.value);
                }};
                eventList.put(promEvent.labelHash, data);
            }
        }

        List<Map<String, Object>> datas = new ArrayList<>();
        eventList.forEach((k, v) -> {
            datas.add(new HashMap<String, Object>() {{
                put("namespace", "dashbase");
                put("labels", v.labels);
                v.value.forEach(this::put);
            }});
        });
        return datas;
    }

    private static String trimZeroOfNumber(Object value) {
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setGroupingUsed(false);
        return fmt.format(value);
    }
}
