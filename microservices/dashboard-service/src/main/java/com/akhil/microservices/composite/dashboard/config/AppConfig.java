package com.akhil.microservices.composite.dashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    @Value("${api.common.title}")
    private String apiTitle;
    @Value("${api.common.description}")
    private String apiDescription;
    @Value("${api.common.version}")
    private String apiVersion;
    @Value("${api.common.contact.name}")
    private String apiContactName;
    @Value("${api.common.contact.url}")
    private String apiContactUrl;
    @Value("${api.common.contact.email}")
    private String apiContactEmail;
    @Value("${api.common.termsOfService}")
    private String apiTermsOfService;
    @Value("${api.common.license}")
    private String apiLicense;
    @Value("${api.common.licenseUrl}")
    private String apiLicenseUrl;
    @Value("${api.common.externalDocDescription}")
    private String apiExternalDocDescription;
    @Value("${api.common.externalDocUrl}")
    private String apiExternalDocUrl;

    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    @Autowired
    public AppConfig(
            @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}") Integer taskQueueSize
    ) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Primary
    ObjectMapper mapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Bean
    public OpenAPI getOpenApiDocumentation() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(apiTitle)
                                .description(apiDescription)
                                .version(apiVersion)
                                .contact(
                                        new Contact()
                                                .name(apiContactName)
                                                .url(apiContactUrl)
                                                .email(apiContactEmail)
                                )
                                .termsOfService(apiTermsOfService)
                                .license(
                                        new License()
                                                .name(apiLicense)
                                                .url(apiLicenseUrl)
                                )
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description(apiExternalDocDescription)
                                .url(apiExternalDocUrl)
                );
    }

    @Bean
    public Scheduler publishEventScheduler() {
        LOG.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
    }

    @Autowired
    private ReactorLoadBalancerExchangeFilterFunction loadBalancerExchangeFilterFunction;

    @Bean
    @LoadBalanced
    public WebClient webClient(WebClient.Builder builder) {
        return builder.filter(loadBalancerExchangeFilterFunction).build();
    }
}
