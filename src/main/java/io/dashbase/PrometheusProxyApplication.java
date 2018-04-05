package io.dashbase;

import io.dashbase.client.http.HttpClientService;
import io.dashbase.web.server.PrometheusResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class PrometheusProxyApplication extends Application<PrometheusConfig> {

    public static HttpClientService httpService;

    public static PrometheusConfig config;

    @Override
    public String getName() {
        return "PrometheusProxy";
    }

    @Override
    public void run(PrometheusConfig prometheusConfig, Environment environment) {
        URL url;

        try {
            url = new URL(prometheusConfig.apiUrl);
        } catch (MalformedURLException mfue) {
            throw new RuntimeException("invalid url: " + prometheusConfig.apiUrl);
        }

        httpService = new HttpClientService(url, null, Optional.ofNullable(prometheusConfig.dashbaseInternalServiceToken));
        config = prometheusConfig;
        environment.jersey().register(new PrometheusResource());
    }

    public static void main(String[] args) throws Exception {
        new PrometheusProxyApplication().run(args);
    }
}
