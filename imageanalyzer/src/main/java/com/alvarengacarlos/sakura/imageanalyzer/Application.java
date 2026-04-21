package com.alvarengacarlos.sakura.imageanalyzer;

import com.alvarengacarlos.sakura.common.ImageMetadataEntity;
import com.alvarengacarlos.sakura.common.ImageMetadataRepository;
import com.alvarengacarlos.sakura.common.ImageMetadataStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.alvarengacarlos.sakura.common")
@EnableJpaRepositories(basePackages = "com.alvarengacarlos.sakura.common")
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private ImageMetadataRepository imageMetadataRepository;

    @Autowired
    private ImageProcessor imageProcessor;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Scheduled(cron = "${job.cron}")
    public void imageAnalyzerJob() {
        log.info("Image analyzer job started");
		
		List<ImageMetadataEntity> pendingImagesMetadata = getPendingImagesMetadata();
        log.info("Found {} image(s) metadata pending analysis", pendingImagesMetadata.size());

        for (ImageMetadataEntity pendingImageMetadata : pendingImagesMetadata) {
			try {
				log.info("Processing image metadata id {}", pendingImageMetadata.getId());
				imageProcessor.processImage(pendingImageMetadata);
				log.info("Successfully processed");

			} catch (Throwable e) {
				log.error("Error to process image metadata: {}", e.getMessage(), e);
			}
        }

        log.info("Image analyzer job finished");
    }
	
	private List<ImageMetadataEntity> getPendingImagesMetadata() {
		log.info("Getting pending images metadata from database");
		List<ImageMetadataEntity> pending = imageMetadataRepository.findByStatus(ImageMetadataStatus.PENDING_ANALYSIS);
		log.info("Successfully got");
		return pending;
	}
}
