package io.dashbase;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Maps;

public class KafkaConfiguration {

    public static final String DEFAULT_KAFKA_CLIENT_ID = "DashbaseKafkaSink";

    @NotEmpty
    public String hosts;

    @NotNull
    public String clientId = DEFAULT_KAFKA_CLIENT_ID;

    public Map<String, String> kafkaProps = Maps.newHashMap();
}
