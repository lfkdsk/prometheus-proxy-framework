package io.dashbase.server.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RangeQuery implements BaseResult.QueryResult {
    /**
     * {
     * "metric" : {
     * "__name__" : "up",
     * "job" : "prometheus",
     * "instance" : "localhost:9090"
     * },
     * "values" : [
     * [ 1435781430.781, "1" ],
     * [ 1435781445.781, "1" ],
     * [ 1435781460.781, "1" ]
     * ]
     * },
     * {
     * "metric" : {
     * "__name__" : "up",
     * "job" : "node",
     * "instance" : "localhost:9091"
     * },
     * "values" : [
     * [ 1435781430.781, "0" ],
     * [ 1435781445.781, "0" ],
     * [ 1435781460.781, "1" ]
     * ]
     */
    private Map<String, String> metric;
    private List<List<String>> value;
}
