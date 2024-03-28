package com.akhil.microservices.composite.dashboard.config;

import com.akhil.microservices.composite.dashboard.services.DashboardCompositeIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfiguration {

    @Autowired
    DashboardCompositeIntegration integration;

    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("account", () -> integration.getAccountHealth());
        registry.put("expense", () -> integration.getExpenseHealth());

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
