package com.cloudnative.tiendaperritos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan

public class TiendaperritosApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiendaperritosApplication.class, args);
	}

}
