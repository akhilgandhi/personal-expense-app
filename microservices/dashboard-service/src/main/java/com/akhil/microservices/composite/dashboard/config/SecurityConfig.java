package com.akhil.microservices.composite.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers("/oauth2/**").permitAll()
                        .pathMatchers("/login/**").permitAll()
                        .pathMatchers("/error/**").permitAll()
                        .pathMatchers("/openapi/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers(POST, "/dashboard/**").hasAuthority("SCOPE_account:write")
                        .pathMatchers(DELETE, "/dashboard/**").hasAuthority("SCOPE_account:write")
                        .pathMatchers(GET, "/dashboard/**").hasAuthority("SCOPE_account:read")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
