package com.akhil.microservices.core.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@ComponentScan("com.akhil")
public class AccountServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(AccountServiceApplication.class);

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		ConfigurableApplicationContext ctx = SpringApplication.run(AccountServiceApplication.class, args);
		String dbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String dbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		LOG.info("Connected to MongoDb: " + dbHost + ":" + dbPort);
	}

}
