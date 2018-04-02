package io.dashbase.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PrometheusEvent {
    public PrometheusEvent setKey(String key) {
        this.key = key;
        return this;
    }

    public PrometheusEvent setValue(Map<String, Object> value) {
        this.value = value;
        return this;
    }

    public PrometheusEvent setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public PrometheusEvent setLabelHash(int labelHash) {
        this.labelHash = labelHash;
        return this;
    }

    @Getter
    public String key;
    @Getter
    public Map<String, Object> value = new HashMap<>();
    @Getter
    public Map<String, String> labels = new HashMap<>();
    @Getter
    public int labelHash;


}