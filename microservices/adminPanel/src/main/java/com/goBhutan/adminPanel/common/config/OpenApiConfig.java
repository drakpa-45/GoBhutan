package com.goBhutan.adminPanel.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    // --- Main OpenAPI object: title, description, version, servers, security ---
    @Bean
    public OpenAPI goBhutanOpenAPI() {
        // Optional: declare a Bearer (JWT) security scheme
        SecurityScheme bearerScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("GoBhutan Admin Panel API")
                        .version("v1")
                        .description("APIs for buses, routes, schedules, and bookings")
                        .contact(new Contact().name("GoBhutan Devs").email("devs@gobhutan.bt"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                       // new Server().url("http://localhost:8085").description("Local")
                        new Server().url("https://gobhutan.site").description("Prod")
                ))
                // comment out these two lines if you don't use JWT
                .schemaRequirement("bearerAuth", bearerScheme)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    // --- Grouping: show only your admin controllers as a separate group in Swagger UI ---
    @Bean
    public GroupedOpenApi busAdminApi() {
        return GroupedOpenApi.builder()
                .group("bus-admin")
                .packagesToScan("com.goBhutan.adminPanel.busAdmin.controller")
                // .pathsToMatch("/api/**") // optional
                .build();
    }
}
