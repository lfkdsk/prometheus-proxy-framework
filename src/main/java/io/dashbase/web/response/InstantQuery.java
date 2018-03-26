package io.dashbase.web.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

//  "resultType": "matrix" | "vector" | "scalar" | "string",

@Data
public class InstantQuery implements BaseResult.QueryResult {
    /**
     * metric : {"__name__":"up","job":"prometheus","instance":"localhost:9090"}
     * value : [1.435781451781E9,"1"]
     */

    private Map<String, String> metric;
    private List<Object> value;
}
