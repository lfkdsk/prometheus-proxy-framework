package io.dashbase.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusGroup {
    @Getter
    @Setter
    public List<PrometheusUtils.Data> group = new ArrayList<>();

    @Getter
    @Setter
    public Map<String, String> labels = new HashMap<>();

    @Getter
    @Setter
    public String namespace = "dashbase";
}
