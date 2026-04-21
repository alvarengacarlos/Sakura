package com.alvarengacarlos.sakura.imageanalyzer;

import com.alvarengacarlos.sakura.common.ImageMetadataEntity;
import com.alvarengacarlos.sakura.common.ImageMetadataRepository;
import com.alvarengacarlos.sakura.common.ImageMetadataStatus;
import com.alvarengacarlos.sakura.common.PaymentMethod;
import com.alvarengacarlos.sakura.common.TaxReceiptEntity;
import com.alvarengacarlos.sakura.common.TaxReceiptRepository;
import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
public class ImageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessor.class);

    @Autowired
    private ImageMetadataRepository imageMetadataRepository;

    @Autowired
    private TaxReceiptRepository taxReceiptRepository;

    @Autowired
    private AnthropicClient anthropicClient;

    @Value("${sakura.anthropic.model}")
    private String model;

    @Transactional
    public void processImage(ImageMetadataEntity imageMetadata) {  
        AnalysisResponse response = analyzeImage(imageMetadata);
                
        if (!Boolean.TRUE.equals(response.isTaxReceipt)) {
            log.warn("Image metadata is not a tax receipt");
            
            log.info("Updating image metadata status to analyzed on database");
            imageMetadata.setStatus(ImageMetadataStatus.ANALYZED);
            imageMetadata.setUpdatedAt(LocalDateTime.now());
            imageMetadataRepository.save(imageMetadata);
            log.info("Successfully updated");

            log.info("Skipping tax receipt save");
            return;
        }

        TaxReceiptEntity receipt = TaxReceiptEntity.builder()
                .id(UUID.randomUUID())
                .description(response.description)
                .quantity(response.quantity)
                .price(response.price)
                .paymentMethod(PaymentMethod.valueOf(response.paymentMethod))
                .storeName(response.where)
                .transactionTime(LocalDateTime.parse(response.when))
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Saving tax receipt on database");
        taxReceiptRepository.save(receipt);
        log.info("Successfully saved");

        log.info("Updating image metadata status to analyzed on database");
        imageMetadata.setStatus(ImageMetadataStatus.ANALYZED);
        imageMetadata.setUpdatedAt(LocalDateTime.now());
        imageMetadataRepository.save(imageMetadata);
        log.info("Successfully updated");
    }

    private AnalysisResponse analyzeImage(ImageMetadataEntity metadata) {
        log.info("Analyzing image");

        byte[] imageBytes = readImage(metadata);
        String mimeType = detectMimeType(metadata.getPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        StructuredMessageCreateParams<AnalysisResponse> params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024L)
                .addUserMessageOfBlockParams(List.of(
                        ContentBlockParam.ofImage(ImageBlockParam.builder()
                                .source(ImageBlockParam.Source.ofBase64(Base64ImageSource.builder()
                                        .mediaType(Base64ImageSource.MediaType.of(mimeType))
                                        .data(base64Image)
                                        .build()))
                                .build()),
                        ContentBlockParam.ofText(TextBlockParam.builder()
                                .text(buildPrompt())
                                .build())
                ))
                .outputConfig(AnalysisResponse.class)
                .build();

        log.info("Calling Claude API");
        StructuredMessage<AnalysisResponse> structuredMessage = anthropicClient.messages().create(params);
        log.info("Successfully called");
        
        log.info("Extracting content");
        AnalysisResponse response = structuredMessage.content().stream()
                .flatMap(cb -> cb.text().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Empty response from Claude"))
                .text();
        log.info("Successfully extracted");

        log.info("Image analyzed with success");

        return response;
    }

    private byte[] readImage(ImageMetadataEntity metadata) {
        try {
                log.info("Reading image from filesystem");
                byte[] image = Files.readAllBytes(Path.of(metadata.getPath()));
                log.info("Successfully read");

                return image;

        } catch (IOException e) {
                throw new RuntimeException(e);
        }
    }

    private String detectMimeType(String path) {
        log.info("Detecting mime type");
        
        String lower = path.toLowerCase();
        String mimeType = "";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
        } else if (lower.endsWith(".png")) {
                mimeType = "image/png";
        } else {
                throw new IllegalArgumentException("Unsupported file type " + path);
        }

        log.info("Successfully detected");
        return mimeType;
    }

    private String buildPrompt() {
        return """
                You are an expert at reading Brazilian fiscal documents.

                Analyze the provided image.

                Set isTaxReceipt to true only if the image is a Brazilian "Cupom Fiscal" \
                (a retail/service tax receipt). Otherwise set it to false and leave all other \
                fields empty.

                If it IS a Cupom Fiscal, extract:
                - description: main item or summary of items purchased
                - quantity: total number of items as an integer
                - price: total amount paid as a decimal (no currency symbols)
                - paymentMethod: map "dinheiro" -> CASH, "crédito"/"cartão de crédito" -> CREDIT_CARD, \
                "débito"/"cartão de débito" -> DEBIT_CARD, "pix" -> PIX. Use CASH if not identifiable.
                - where: name of the establishment
                - when: transaction date/time in ISO-8601 format (use T00:00:00 if time is not visible)
                """;
    }
}
