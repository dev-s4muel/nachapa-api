package com.nachapa.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String TITTLE = "NeoApp technical challenge API";
    private static final String VERSION = "1.0";
    private static final String DESCRIPTION = "MVP REST API for customer registration (NeoApp technical challenge - Spring Boot)";
    private static final String BEARER_AUTH = "bearerAuth";
    private static final String BEARER = "bearer";
    private static final String BEARER_FORMAT = "JWT";


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITTLE)
                        .version(VERSION)
                        .description(DESCRIPTION))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH)) // Define o esquema de segurança global
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, // Nome do esquema de segurança
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP) // Tipo HTTP
                                        .scheme(BEARER) // Bearer Token
                                        .bearerFormat(BEARER_FORMAT) // Formato JWT
                        ));
    }
}
