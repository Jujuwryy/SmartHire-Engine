package com.george.config;

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
    public OpenAPI smartHireOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.smarthire.example.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("support@smarthire.example.com");
        contact.setName("SmartHire Support");

        License mitLicense = new License()
            .name("MIT License")
            .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
            .title("SmartHire Engine API")
            .version("1.0.0")
            .contact(contact)
            .description("Advanced AI-powered job matching system using vector embeddings. " +
                "This API provides endpoints for generating embeddings from job postings and " +
                "finding matching jobs based on user profiles using semantic similarity search.")
            .license(mitLicense);

        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer, prodServer));
    }
}

