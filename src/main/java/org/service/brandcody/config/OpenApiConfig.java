package org.service.brandcody.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI brandCodyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("브랜드 코디 서비스 API")
                        .description("카테고리별 최저가격 브랜드를 조회하고 브랜드와 상품을 관리하는 API 문서입니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Jaehyun Kim")
                                .url("https://github.com/jae-kimSeo/brand-cody")
                                .email("vibobby88@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("브랜드 코디 서비스 README")
                        .url("https://github.com/jae-kimSeo/brand-cody"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버 (8080)")
                ))
                .components(new Components());
    }
}