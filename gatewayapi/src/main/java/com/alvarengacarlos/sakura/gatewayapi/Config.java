package com.alvarengacarlos.sakura.gatewayapi;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@Getter
public class Config {

    @Value("${sakura.storage.images-path}")
    private Path imagesPath;
}
