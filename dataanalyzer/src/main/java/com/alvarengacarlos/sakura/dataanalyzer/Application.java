package com.alvarengacarlos.sakura.dataanalyzer;

import com.alvarengacarlos.sakura.common.TaxReceiptEntity;
import com.alvarengacarlos.sakura.common.TaxReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.alvarengacarlos.sakura.common")
@EnableJpaRepositories(basePackages = "com.alvarengacarlos.sakura.common")
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private TaxReceiptRepository taxReceiptRepository;

    @Autowired
    private DataProcessor dataAnalyzer;

    @Value("${sakura.analysis.days}")
    private int analysisDays;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Scheduled(cron = "${job.cron}")
    public void dataAnalyzerJob() {
        log.info("Data analyzer job started");
        
        List<TaxReceiptEntity> taxReceipts = getTaxReceipts();
        log.info("Found {} receipt(s) in the last {} days", taxReceipts.size(), analysisDays);

        if (taxReceipts.size() == 0) {
            log.info("No data to process, job finished");
            return;
        }

        try {
            log.info("Processing data");
            dataAnalyzer.processData(taxReceipts);
            log.info("Successfully processed");
        } catch (Throwable e) {
            log.error("Error during data processing: {}", e.getMessage(), e);
        }

        log.info("Data analyzer job finished");
    }

    private List<TaxReceiptEntity> getTaxReceipts() {
        log.info("Getting tax receipts from the last {} days", analysisDays);
        List<TaxReceiptEntity> receipts = taxReceiptRepository.findByTransactionTimeAfter(LocalDateTime.now().minusDays(analysisDays));
        log.info("Successfully got");
        return receipts;
    }
}
