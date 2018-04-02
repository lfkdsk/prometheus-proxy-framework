package io.dashbase.web.server;


import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;

public class utils {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'hh:mm:ss.SSSZZ'");

    public static ObjectNode addInformation(Map data) {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("@timestamp", getCurrentTime());

        ObjectNode metadata = JsonNodeFactory.instance.objectNode();
        metadata.put("type", "doc")
                .put("version", "0.0.1")
                .put("topic", "dashbase");

        result.set("@metadata", metadata);

        ObjectNode metricset = JsonNodeFactory.instance.objectNode();
        metricset.put("name", "collector")
                 .put("module", "prometheus")
                 .put("host", "localhost:9090")
                 .put("rtt", 226)
                 .put("namespace", "prometheus.dashbase");

        result.set("metricset", metricset);

        ObjectNode prometheus = JsonNodeFactory.instance.objectNode();
        prometheus.putPOJO("dashbase", data);

        result.set("prometheus", prometheus);

        return result;
    }

    private static String getCurrentTime() {
        return dateTimeFormatter.print(System.currentTimeMillis());
    }
}
