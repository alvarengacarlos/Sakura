package com.alvarengacarlos.sakura.gatewayapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.alvarengacarlos.sakura.common")
@EnableJpaRepositories(basePackages = "com.alvarengacarlos.sakura.common")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
