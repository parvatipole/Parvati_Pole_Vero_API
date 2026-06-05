package com.vero.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vero API — transaction management backend.
 *
 * Entry point for the Spring Boot application. Configuration is loaded from
 * application.properties. The H2 console is available at /h2-console during
 * local development.
 */
@SpringBootApplication
public class VeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeroApplication.class, args);
    }
}
