package com.akhil.microservices.core.goal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.akhil")
public class GoalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoalServiceApplication.class, args);
	}

}
