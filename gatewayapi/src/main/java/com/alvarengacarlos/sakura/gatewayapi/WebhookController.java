package com.alvarengacarlos.sakura.gatewayapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;
    private final Config config;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        log.info("Verifying webhook verification request with mode: {}", mode);
        if ("subscribe".equals(mode) && config.getWhatsAppVerifyToken().equals(token)) {
            log.info("Successfully verified");
            return challenge;
        }
        log.warn("Invalid verify token");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verify token");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void process(@RequestBody @Valid WhatsAppWebhookRequestDto request) {
        log.info("{}", request);
        log.info("Processing webhook payload");
        webhookService.process(request);
        log.info("Successfully processed");
    }
}
