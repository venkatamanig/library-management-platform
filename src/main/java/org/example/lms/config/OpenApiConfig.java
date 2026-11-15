
package org.example.lms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management System API")
                        .version("1.0.0")
                        .description("REST API for authentication, catalogue, circulation, memberships, fines and administration."))
                .servers(java.util.List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("LMS Gateway")
                ))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return group("authentication", "/auth/**");
    }

    @Bean
    public GroupedOpenApi booksApi() {
        return group("books-and-copies", "/book", "/book/**", "/books/**");
    }

    @Bean
    public GroupedOpenApi patronsApi() {
        return group("patrons", "/patron/**", "/patron-management/**");
    }

    @Bean
    public GroupedOpenApi borrowingApi() {
        return group("borrowing", "/borrow/**");
    }

    @Bean
    public GroupedOpenApi finesApi() {
        return group("fines", "/fines/**");
    }

    @Bean
    public GroupedOpenApi membershipsApi() {
        return group("memberships", "/memberships/**");
    }

    @Bean
    public GroupedOpenApi notificationsApi() {
        return group("notifications", "/notifications/**");
    }

    @Bean
    public GroupedOpenApi reportsApi() {
        return group("reports", "/reports/**");
    }

    @Bean
    public GroupedOpenApi administrationApi() {
        return group("administration", "/admin/**");
    }

    private GroupedOpenApi group(String name, String... paths) {
        return GroupedOpenApi.builder().group(name).pathsToMatch(paths).build();
    }
}
