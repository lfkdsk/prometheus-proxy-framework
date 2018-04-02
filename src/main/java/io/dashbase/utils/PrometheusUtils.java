package io.dashbase.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hawkular.agent.prometheus.types.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusUtils {


    public static List<PrometheusEvent> toPromEvents(MetricFamily metricFamily) {
        String name = metricFamily.getName();
        List<PrometheusEvent> prometheusEvents = new ArrayList<>();
        for (Metric metric : metricFamily.getMetrics()) {
            PrometheusEvent prometheusEvent = new PrometheusEvent();
            prometheusEvent.setKey(name);
            prometheusEvent.setLabelHash("#".hashCode());

            Map<String, Object> value = new HashMap<>();

            Map<String, String> labels = metric.getLabels();

            if (!labels.isEmpty()) {
                Map<String, String> tagsMap = new HashMap<>();

                labels.forEach((k, v) -> {
                    if (!k.isEmpty() && !v.isEmpty()) {
                        tagsMap.put(k, v);
                    }
                });
                prometheusEvent.labels = tagsMap;
                prometheusEvent.labelHash = tagsMap.hashCode();
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

            prometheusEvent.value = value;

            prometheusEvents.add(prometheusEvent);

        }
        return prometheusEvents;
    }

    public static class Data {
        @Getter
        @Setter
        @JsonProperty()
        Map<String, Object> value;
    }

    public static List<Map<String, Object>> getDatas(List<MetricFamily> metricFamilies) {
        Map<Integer, PrometheusGroup> groupList = new HashMap<>();
        for (MetricFamily metricFamily : metricFamilies) {
            for (PrometheusEvent prometheusEvent : toPromEvents(metricFamily)) {
                Data data = new Data();

                if (!groupList.containsKey(prometheusEvent.labelHash)) {
                    groupList.put(prometheusEvent.labelHash, new PrometheusGroup());
                }

                if (!prometheusEvent.labels.isEmpty()) {
                    groupList.get(prometheusEvent.labelHash).labels = prometheusEvent.labels;
                }

                data.value = new HashMap<String, Object>() {{
                    put(prometheusEvent.key, prometheusEvent.value);
                }};

                groupList.get(prometheusEvent.labelHash).group.add(data);
            }
        }

        List<Map<String, Object>> datas = new ArrayList<>();
        groupList.forEach((k, v) -> {
            datas.add(new HashMap<String, Object>() {{
                put("namespace", v.namespace);
                if (v.labels != null)
                    put("labels", v.labels);
                v.group.forEach(data -> data.value.forEach(this::put));
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
