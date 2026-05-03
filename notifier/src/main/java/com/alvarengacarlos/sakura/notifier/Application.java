package com.alvarengacarlos.sakura.notifier;

import com.alvarengacarlos.sakura.common.DataAnalysisEntity;
import com.alvarengacarlos.sakura.common.DataAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.alvarengacarlos.sakura.common")
@EnableJpaRepositories(basePackages = "com.alvarengacarlos.sakura.common")
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private DataAnalysisRepository dataAnalysisRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${notifier.recipient-email}")
    private String recipientEmail;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Scheduled(cron = "${job.cron}")
    public void notifierJob() {
        log.info("Notifier job started");

        LocalDate today = LocalDate.now();
        List<DataAnalysisEntity> records = getRecordsForDate(today);
        log.info("Found {} record(s) for {}", records.size(), today);

        if (records.isEmpty()) {
            log.info("No records for today, skipping email. Job finished");
            return;
        }

        try {
			log.info("Sending email to {}", recipientEmail);
            sendEmail(today, records);
            log.info("Successfully sent");
        } catch (MailException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }

        log.info("Notifier job finished");
    }

    private List<DataAnalysisEntity> getRecordsForDate(LocalDate date) {
        log.info("Querying data_analysis for next_purchase_date = {}", date);
        List<DataAnalysisEntity> records = dataAnalysisRepository.findByNextPurchaseDate(date);
        log.info("Successfully queried");
        return records;
    }

    private void sendEmail(LocalDate date, List<DataAnalysisEntity> records) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Shopping List for " + date);
        message.setText(buildEmailBody(records));
        mailSender.send(message);
    }

    private String buildEmailBody(List<DataAnalysisEntity> records) {
		log.info("Building email content");

        StringBuilder sb = new StringBuilder("Items to purchase today:\n\n");
        for (DataAnalysisEntity record : records) {
            sb.append("- %s, qty: %d, unit: %s\n".formatted(
                record.getItem(),
                record.getQuantity(),
                record.getMeasureUnit()
            ));
        }

		String content = sb.toString();

		log.info("Successfully built");
		return content;
    }
}
