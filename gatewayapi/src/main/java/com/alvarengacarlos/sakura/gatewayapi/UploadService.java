package com.alvarengacarlos.sakura.gatewayapi;

import com.alvarengacarlos.sakura.common.ImageMetadataEntity;
import com.alvarengacarlos.sakura.common.ImageMetadataRepository;
import com.alvarengacarlos.sakura.common.ImageMetadataStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
public class UploadService {

    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png");

    private final ImageMetadataRepository imageMetadataRepository;
    private final Config config;

    public void upload(MultipartFile file) {
        String contentType = file.getContentType();
        String extension = ALLOWED_MIME_TYPES.get(contentType);
        if (extension == null) {
            log.warn("Unsupported MIME type {}", contentType);
            throw new UnsupportedImageTypeException("Only JPEG and PNG images are accepted");
        }

        Path filePath = config.getImagesPath().resolve(UUID.randomUUID() + extension);

        try {
            log.info("Saving image on filesystem");
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            log.info("Successfully saved on filesystem");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info("Saving metadata on database");
        LocalDateTime now = LocalDateTime.now();
        imageMetadataRepository.save(
                new ImageMetadataEntity(UUID.randomUUID(), filePath.toString(), ImageMetadataStatus.PENDING_ANALYSIS,
                        now, now));
        log.info("Successfully saved on database");
    }
}
