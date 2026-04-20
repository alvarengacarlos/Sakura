package com.alvarengacarlos.sakura.gatewayapi;

import com.alvarengacarlos.sakura.common.ImageMetadataRepository;
import com.alvarengacarlos.sakura.common.ImageMetadataEntity;
import com.alvarengacarlos.sakura.common.ImageMetadataStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png");

    private final ImageMetadataRepository imageMetadataRepository;
    private final RestClient restClient;
    private final Config config;

    public void process(WhatsAppWebhookRequestDto request) {
        WhatsAppWebhookRequestDto.Message message = request.entry().get(0).changes().get(0).value().messages().get(0);
        log.info("Processing message with ID: {}", message.id());

        if (!"image".equals(message.type())) {
            log.warn("Only image are accepted, received {}", message.type());
            throw new NonImageMessageException("Only image messages are accepted");
        }

        WhatsAppWebhookRequestDto.ImageData imageData = message.image();

        if (imageData.url() == null || !imageData.url().startsWith(config.getGraphApiBaseUrl())) {
            log.warn("Invalid image URL: {}", imageData.url());
            throw new IllegalArgumentException("Invalid image URL");
        }

        String extension = ALLOWED_MIME_TYPES.get(imageData.mimeType());
        if (extension == null) {
            log.warn("Unsupported MIME type {}", imageData.mimeType());
            throw new NonImageMessageException("Only JPEG and PNG images are accepted");
        }

        Path filePath = config.getImagesPath().resolve(message.id() + extension);

        log.info("Getting image");
        byte[] imageBytes = restClient.get()
                .uri(imageData.url())
                .header("Authorization", "Bearer " + config.getWhatsAppAccessToken())
                .retrieve()
                .body(byte[].class);
        log.info("Successfully got image");

        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, imageBytes);
            log.info("Successfully saved image");
        } catch (IOException e) {
            log.error("Failed to save image");
            throw new UncheckedIOException(e);
        }

        LocalDateTime now = LocalDateTime.now();
        imageMetadataRepository.save(
                new ImageMetadataEntity(UUID.randomUUID(), filePath.toString(), ImageMetadataStatus.PENDING_ANALYSIS,
                        now, now));
        log.info("Successfully persisted metadata");
    }
}
