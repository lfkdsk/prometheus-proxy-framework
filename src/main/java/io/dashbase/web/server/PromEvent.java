package io.dashbase.web.server;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PromEvent {
    public PromEvent setKey(String key) {
        this.key = key;
        return this;
    }

    public PromEvent setValue(Map<String, Object> value) {
        this.value = value;
        return this;
    }

    public PromEvent setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public PromEvent setLabelHash(int labelHash) {
        this.labelHash = labelHash;
        return this;
    }

    @Getter
    String key;
    @Getter
    Map<String, Object> value = new HashMap<>();
    @Getter
    Map<String, String> labels = new HashMap<>();
    @Getter
    int labelHash;


}