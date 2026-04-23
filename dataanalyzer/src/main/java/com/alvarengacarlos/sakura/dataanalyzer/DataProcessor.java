package com.alvarengacarlos.sakura.dataanalyzer;

import com.alvarengacarlos.sakura.common.DataAnalysisEntity;
import com.alvarengacarlos.sakura.common.DataAnalysisRepository;
import com.alvarengacarlos.sakura.common.TaxReceiptEntity;
import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataProcessor {

    private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

    @Autowired
    private DataAnalysisRepository dataAnalysisRepository;

    @Autowired
    private AnthropicClient anthropicClient;

    @Value("${sakura.anthropic.model}")
    private String model;

    @Value("${sakura.analysis.days}")
    private int analysisDays;

    @Transactional
    public void processData(List<TaxReceiptEntity> taxReceipts) {
        PredictionResponse response = analyzeData(taxReceipts);

        List<DataAnalysisEntity> predictions = response.predictions.stream()
                .map(p -> DataAnalysisEntity.builder()
                        .id(UUID.randomUUID())
                        .item(p.item)
                        .quantity(p.quantity)
                        .nextPurchaseDate(LocalDate.parse(p.nextPurchaseDate))
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        log.info("Saving {} prediction(s) on database", predictions.size());
        dataAnalysisRepository.saveAll(predictions);
        log.info("Successfully saved");
    }

    private PredictionResponse analyzeData(List<TaxReceiptEntity> taxReceipts) {
        log.info("Analyzing data");

        StructuredMessageCreateParams<PredictionResponse> params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024L)
                .addUserMessage(buildPrompt(taxReceipts))
                .outputConfig(PredictionResponse.class)
                .build();

        log.info("Calling Claude API");
        StructuredMessage<PredictionResponse> structuredMessage = anthropicClient.messages().create(params);
        log.info("Successfully called");

        log.info("Extracting content");
        PredictionResponse response = structuredMessage.content().stream()
                .flatMap(cb -> cb.text().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Empty response from Claude"))
                .text();
        log.info("Successfully extracted");

        log.info("Data analyzed with success");
        
        return response;
    }

    private String buildPrompt(List<TaxReceiptEntity> taxReceipts) {
        String formattedData = formatTaxReceipts(taxReceipts);

        return """
                You are a spending analyst. Based on the purchase history below, predict the next \
                basket items the person is likely to buy.

                For each predicted item, provide:
                - item: the product or item name
                - quantity: expected quantity as an integer
                - nextPurchaseDate: predicted purchase date in ISO-8601 format (YYYY-MM-DD)

                Purchase history (last %d days):
                """.formatted(analysisDays) + formattedData;
    }

    private String formatTaxReceipts(List<TaxReceiptEntity> taxReceipts) {
        log.info("Formatting tax receipts");

        StringBuilder sb = new StringBuilder();
        for (TaxReceiptEntity taxReceipt : taxReceipts) {
            sb.append("- %s, qty: %s, date: %s\n".formatted(
                taxReceipt.getDescription(),
                taxReceipt.getQuantity(),
                taxReceipt.getTransactionTime()
            ));
        }
        String formattedTaxReceipts = sb.toString();

        log.info("Successfully formatted");

        return formattedTaxReceipts;
    }
}
