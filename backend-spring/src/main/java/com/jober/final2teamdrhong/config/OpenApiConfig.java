package com.jober.final2teamdrhong.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Value("${app.swagger.server-url}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("notimo API Documentation")
                .description("notimo 백엔드 API 문서입니다.")
                .version("v1.0")
                .contact(new Contact()
                        .name("notimo Development Team")
                        .email("dev@notimo.com"));

        // 환경별 서버 URL 설정
        Server server = new Server()
                .url(serverUrl)
                .description("API 서버");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}