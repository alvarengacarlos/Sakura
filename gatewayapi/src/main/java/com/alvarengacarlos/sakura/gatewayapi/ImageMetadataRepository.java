package com.alvarengacarlos.sakura.gatewayapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadataEntity, UUID> {
}
