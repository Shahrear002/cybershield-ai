package com.cybershield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * CyberShield AI — application entry point.
 *
 * <p>Starts an embedded Tomcat server on port {@code 8080} (configurable via
 * {@code application.yml}) and auto-configures all Spring beans discovered
 * on the {@code com.cybershield} base package scan.
 *
 * <h2>Quick-start</h2>
 * <pre>{@code
 *   ./mvnw spring-boot:run
 *   # or
 *   java -jar target/cybershield-ai-0.1.0-SNAPSHOT.jar
 * }</pre>
 *
 * <h2>Key endpoints</h2>
 * <ul>
 *   <li>{@code POST http://localhost:8080/api/triage/classify}  — classify transcript</li>
 *   <li>{@code POST http://localhost:8080/api/evidence/secure}  — anchor evidence file</li>
 *   <li>{@code GET  http://localhost:8080/h2-console}           — H2 dev console</li>
 * </ul>
 */
@SpringBootApplication
public class CybershieldApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CybershieldApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CybershieldApplication.class, args);
    }
}
