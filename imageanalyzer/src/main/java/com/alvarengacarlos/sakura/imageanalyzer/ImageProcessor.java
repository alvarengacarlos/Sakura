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

        log.info("Preparing tax receipts");
        List<TaxReceiptEntity> receipts = response.items.stream()
                .map(item -> TaxReceiptEntity.builder()
                        .id(UUID.randomUUID())
                        .description(item.description)
                        .quantity(item.quantity)
                        .measureUnit(item.measureUnit)
                        .price(item.price)
                        .paymentMethod(PaymentMethod.valueOf(item.paymentMethod))
                        .storeName(item.where)
                        .transactionTime(LocalDateTime.parse(item.when))
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();
        log.info("Successfully prepared");

        log.info("Saving tax receipt items on database");
        taxReceiptRepository.saveAll(receipts);
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
                .maxTokens(4096L)
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

                Set isTaxReceipt to true only if the image is a Brazilian "Cupom Fiscal"
                (a retail/service tax receipt). Otherwise set it to false and leave all other
                fields empty.

                If it IS a Cupom Fiscal, populate items with one entry per product line item. For each item extract:
                - description: product name (ITEM DESCRICAO).
                - quantity: quantity as a decimal number; Brazilian receipts use comma (,) as the decimal separator (e.g. "1,5" means 1.5) — always output using dot (.) as the decimal separator. Eg: 1.5 KG = 1.5, 1 PACOTE = 1, 1 UNID = 1, 1 MCO = 1, 1.5 LT = 1.5, 1 CART = 1.
                - measureUnit: unit of measurement of the quantity as a string. E.g: KG, 1 PACOTE = PACOTE, 1 UNID = UNID, 1 MCO = MCO, 1.5 LT = LT, 1 CART = CART.
                - price: price of this item as a decimal (no currency symbols); Brazilian receipts use comma (,) as the decimal separator (e.g. "1,50" means 1.50) — always output using dot (.) as the decimal separator.
                - paymentMethod: map "dinheiro" -> CASH, "crédito"/"cartão de crédito" -> CREDIT_CARD, "débito"/"cartão de débito" -> DEBIT_CARD, "pix" -> PIX. Use CASH if not identifiable. Repeat the same value for every item.
                - where: name of the establishment. Repeat the same value for every item.
                - when: transaction date/time in ISO-8601 format (use yyyy-mm-ddT00:00:00 if time is not visible). Repeat the same value for every item.
                """;
    }
}
