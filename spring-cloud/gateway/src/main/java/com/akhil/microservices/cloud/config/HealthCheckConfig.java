package com.akhil.microservices.cloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.logging.Level.FINE;

@Configuration
public class HealthCheckConfig {

    public static final Logger LOG = LoggerFactory.getLogger(HealthCheckConfig.class);

    private final WebClient client;

    @Autowired
    public HealthCheckConfig(WebClient.Builder client) {
        this.client = client.build();
    }

    @Bean
    ReactiveHealthContributor healthCheckMicroservices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("account", () -> getHealth("http://account"));
        registry.put("expense", () -> getHealth("http://expense"));
        registry.put("dashboard", () -> getHealth("http://dashboard"));
        registry.put("auth-server", () -> getHealth("http://auth-server"));

        return CompositeReactiveHealthContributor.fromMap(registry);
    }

    private Mono<Health> getHealth(String baseUrl) {
        String url = baseUrl + "/actuator/health";
        LOG.debug("Setting up a call to the Health API on URL: {}", url);
        return client.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), FINE);
    }
}
