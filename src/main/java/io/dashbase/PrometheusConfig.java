package io.dashbase;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

public final class PrometheusConfig extends Configuration {
    @JsonProperty
    @NotNull
    public String apiUrl;

    @JsonProperty("dashbaseInternalServiceToken")
    public String dashbaseInternalServiceToken;

    @NotNull
    @Getter
    public String job;

    @NotNull
    @Getter
    public String instance;
}
