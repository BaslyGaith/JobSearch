package com.jobfinder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI jobFinderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JobFinder AI API")
                        .version("1.0.0")
                        .description("REST API for JobFinder AI — AI-powered job search application")
                        .contact(new Contact()
                                .name("JobFinder AI Team")
                                .email("support@jobfinder.ai"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ));
    }
}
