package io.dashbase;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public final class PrometheusProxyConfiguration extends Configuration {

    @Valid
    @NotNull
    public PrometheusConfiguration prometheus = new PrometheusConfiguration();

    public String outputFormat = "raw";

    @Valid
    @NotNull
    public KafkaConfiguration kafka = new KafkaConfiguration();
}
