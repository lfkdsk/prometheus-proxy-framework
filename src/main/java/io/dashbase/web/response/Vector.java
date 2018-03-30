package io.dashbase.web.response;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dashbase.parser.ast.value.Values;
import lombok.Data;

import java.util.List;
import java.util.Map;

//  "resultType": "matrix" | "vector" | "scalar" | "string",
// for Instant Query
@Data
public class Vector implements Values.Value {

    @JsonValue
    private List<Sample> samples = Lists.newArrayList();

    @Data
    public static class Sample {
        /**
         * metric : {"__name__":"up","job":"prometheus","instance":"localhost:9090"}
         * value : [1.435781451781E9,"1"]
         */

        @JsonAnySetter
        private Map<String, String> metric = Maps.newHashMap();
        @JsonAnySetter
        private List<Object> value = Lists.newArrayList();
    }

    public static Vector of() {
        return new Vector();
    }
}
