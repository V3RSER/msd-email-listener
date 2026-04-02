
package com.example.demo;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;

@SpringBootApplication
@DependsOn("flyway")
public class DemoApplication {

    public DemoApplication(Flyway flyway) {
        // The Flyway bean is injected here to ensure migrations are complete before the application starts
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
