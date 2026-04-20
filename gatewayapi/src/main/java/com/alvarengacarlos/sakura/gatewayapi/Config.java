package com.alvarengacarlos.sakura.gatewayapi;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;

@Configuration
@Getter
public class Config {

    @Value("${sakura.storage.images-path}")
    private Path imagesPath;

    @Value("${sakura.whatsapp.access-token}")
    private String whatsAppAccessToken;

    @Value("${sakura.whatsapp.verify-token}")
    private String whatsAppVerifyToken;

    @Value("${sakura.whatsapp.graph-api-base-url}")
    private String graphApiBaseUrl;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
