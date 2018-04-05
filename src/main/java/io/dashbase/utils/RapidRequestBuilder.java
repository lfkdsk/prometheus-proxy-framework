package io.dashbase.utils;

import lombok.Getter;
import rapid.api.AggregationRequest;
import rapid.api.RapidRequest;
import rapid.api.TimeRangeFilter;
import rapid.api.query.Conjunction;
import rapid.api.query.Query;

import java.util.Arrays;
import java.util.Set;

public final class RapidRequestBuilder {

    @Getter
    private RapidRequest request = new RapidRequest();

    private RapidRequestBuilder() { }

    public static RapidRequestBuilder builder() {
        return new RapidRequestBuilder();
    }

    public RapidRequestBuilder setNumResults(int numResults) {
        this.request.numResults = numResults;
        return this;
    }

    public RapidRequestBuilder setTableNames(Set<String> tableNames) {
        this.request.tableNames.addAll(tableNames);
        return this;
    }

    public RapidRequestBuilder addTableName(String... tableNames) {
        this.request.tableNames.addAll(Arrays.asList(tableNames));
        return this;
    }

    public RapidRequestBuilder addFields(String... fields) {
        this.request.fields.addAll(Arrays.asList(fields));
        return this;
    }

    public RapidRequestBuilder addQuery(Query query) {
        this.request.query = query;
        return this;
    }

    public RapidRequestBuilder conjunctionQuery(Query query) {
        this.request.query = new Conjunction(Arrays.asList(this.request.query, query));
        return this;
    }

    public RapidRequestBuilder setTimeRangeFilter(long start, long end) {
        this.request.timeRangeFilter = new TimeRangeFilter(start, end);
        return this;
    }

    public RapidRequestBuilder addAggregation(String name, AggregationRequest request) {
        this.request.aggregations.put(name, request);
        return this;
    }

    public RapidRequestBuilder setUseApproximation(boolean useApproximation) {
        this.request.useApproximation = useApproximation;
        return this;
    }

    public RapidRequestBuilder setContext(String context) {
        this.request.ctx = context;
        return this;
    }

    public RapidRequestBuilder setFetchSchema(boolean fetchSchema) {
        this.request.fetchSchema = fetchSchema;
        return this;
    }

    public RapidRequestBuilder setTimeoutMillis(int timeoutMillis) {
        this.request.timeoutMillis = timeoutMillis;
        return this;
    }

    public RapidRequestBuilder setDisableHightlight(boolean disableHightlight) {
        this.request.disableHighlight = disableHightlight;
        return this;
    }

    public RapidRequestBuilder setStartId(String startId) {
        this.request.startId = startId;
        return this;
    }

    public RapidRequestBuilder setEndId(String endId) {
        this.request.endId = endId;
        return this;
    }

    public RapidRequestBuilder setId(String startId, String endId) {
        this.request.startId = startId;
        this.request.endId = endId;
        return this;
    }

    public RapidRequestBuilder setDebugMode(int debugMode) {
        this.request.debugMode = debugMode;
        return this;
    }

    public RapidRequest create() {
        return this.request;
    }
}
