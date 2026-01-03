package com.app.pdfstation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pdfStationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PDFStation API")
                        .description(
                                "High-performance PDF processing service with industry-standard compression, merging, and manipulation capabilities")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PDFStation Team")
                                .email("support@pdfstation.com")
                                .url("https://github.com/yourusername/pdfstation"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development Server")));
    }
}
