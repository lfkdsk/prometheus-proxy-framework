package io.dashbase;

import com.google.common.collect.Sets;
import io.dashbase.client.http.HttpClientService;
import io.dashbase.web.server.PrometheusResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import rapid.api.RapidRequest;
import rapid.api.RapidResponse;
import rapid.api.TimeRangeFilter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class PrometheusProxyApplication extends Application<PrometheusConfig> {

    public static HttpClientService httpService;

    @Override
    public String getName() {
        return "PrometheusProxy";
    }

    @Override
    public void run(PrometheusConfig prometheusConfig, Environment environment) throws Exception {
        URL url;

        try {
            url = new URL(prometheusConfig.apiUrl);
        } catch (MalformedURLException mfue) {
            throw new RuntimeException("invalid url: " + prometheusConfig.apiUrl);
        }

        httpService = new HttpClientService(url, null, Optional.ofNullable(prometheusConfig.dashbaseInternalServiceToken));

        environment.jersey().register(new PrometheusResource());

        RapidRequest rapidRequest = new RapidRequest();
        rapidRequest.tableNames = Sets.newHashSet("_metrics");
        rapidRequest.fields = Sets.newHashSet("jvm.cpu.usage.percent.value");
        rapidRequest.timeRangeFilter = new TimeRangeFilter();
        rapidRequest.timeRangeFilter.startTimeInSec = 1522757895;
        rapidRequest.timeRangeFilter.endTimeInSec = 1522757896;
        RapidResponse response = httpService.query(rapidRequest);
        System.out.println(response);
    }

    public static void main(String[] args) throws Exception {
        new PrometheusProxyApplication().run(args);
    }
}
