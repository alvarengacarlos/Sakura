package com.alvarengacarlos.sakura.common;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadataEntity, UUID> {
    List<ImageMetadataEntity> findByStatus(ImageMetadataStatus status);
}
