package io.dashbase;

import io.dashbase.web.server.PrometheusResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class PrometheusProxyApplication extends Application<PrometheusConfig> {

    @Override
    public String getName() {
        return "PrometheusProxy";
    }

    @Override
    public void run(PrometheusConfig prometheusConfig, Environment environment) throws Exception {
        environment.jersey().register(new PrometheusResource());
    }

    public static void main(String[] args) throws Exception {
        new PrometheusProxyApplication().run(args);
    }
}
