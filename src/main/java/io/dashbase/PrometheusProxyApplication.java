package io.dashbase;

import io.dashbase.web.server.MetricsResource;
import io.dashbase.web.server.PrometheusResource;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusProxyApplication extends Application<PrometheusProxyConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusProxyApplication.class);


    @Override
    public String getName() {
        return "PrometheusProxy";
    }

    public void initialize(final Bootstrap<PrometheusProxyConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()));
    }

    @Override
    public void run(PrometheusProxyConfiguration configuration, Environment environment) throws Exception {

        KafkaSink kafkaSink = new KafkaSink(configuration.kafka, environment.metrics());

        environment.jersey().register(new PrometheusResource());


        environment.jersey().register(new MetricsResource(kafkaSink, configuration.outputFormat, environment.metrics()));
    }

    public static void main(String[] args) throws Exception {
        new PrometheusProxyApplication().run(args);
    }
}
