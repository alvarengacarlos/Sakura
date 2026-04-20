package com.alvarengacarlos.sakura.gatewayapi;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebhookServiceTest {

    @Mock
    private ImageMetadataRepository imageMetadataRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private Config config;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private WebhookService webhookService;

    @TempDir
    Path tempDir;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(byte[].class)).thenReturn(new byte[] { 1, 2, 3 });
        when(config.getImagesPath()).thenReturn(tempDir);
        when(config.getWhatsAppAccessToken()).thenReturn("test-token");
        when(config.getGraphApiBaseUrl()).thenReturn("https://graph.facebook.com/");
    }

    @Nested
    class ShouldThrowNonImageMessageException {

        @Test
        void whenMessageTypeIsNotImage() {
            WhatsAppWebhookRequestDto request = Instancio.of(WhatsAppWebhookRequestDto.class)
                    .withMaxDepth(15)
                    .set(field(WhatsAppWebhookRequestDto.Message.class, "type"), "text")
                    .create();

            assertThrows(NonImageMessageException.class, () -> webhookService.process(request));
        }

        @Test
        void whenMimeTypeIsNotAllowed() {
            WhatsAppWebhookRequestDto request = Instancio.of(WhatsAppWebhookRequestDto.class)
                    .withMaxDepth(15)
                    .set(field(WhatsAppWebhookRequestDto.Message.class, "type"), "image")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "mimeType"), "image/gif")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "url"), "https://graph.facebook.com/v19.0/12345")
                    .create();

            assertThrows(NonImageMessageException.class, () -> webhookService.process(request));
        }

        @Test
        void whenUrlIsInvalid() {
            WhatsAppWebhookRequestDto request = Instancio.of(WhatsAppWebhookRequestDto.class)
                    .withMaxDepth(15)
                    .set(field(WhatsAppWebhookRequestDto.Message.class, "type"), "image")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "url"), "https://malicious.com/v19.0/12345")
                    .create();

            assertThrows(IllegalArgumentException.class, () -> webhookService.process(request));
        }
    }

    @Nested
    class ShouldSaveFileAndPersistMetadata {

        @Test
        void whenJpegImage() {
            WhatsAppWebhookRequestDto request = Instancio.of(WhatsAppWebhookRequestDto.class)
                    .withMaxDepth(15)
                    .set(field(WhatsAppWebhookRequestDto.Message.class, "type"), "image")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "mimeType"), "image/jpeg")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "url"), "https://graph.facebook.com/v19.0/12345")
                    .create();
            String messageId = request.entry().get(0).changes().get(0).value().messages().get(0).id();

            webhookService.process(request);

            assertTrue(Files.exists(tempDir.resolve(messageId + ".jpg")));
            verify(imageMetadataRepository, times(1)).save(any(ImageMetadataEntity.class));
        }

        @Test
        void whenPngImage() {
            WhatsAppWebhookRequestDto request = Instancio.of(WhatsAppWebhookRequestDto.class)
                    .withMaxDepth(15)
                    .set(field(WhatsAppWebhookRequestDto.Message.class, "type"), "image")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "mimeType"), "image/png")
                    .set(field(WhatsAppWebhookRequestDto.ImageData.class, "url"), "https://graph.facebook.com/v19.0/12345")
                    .create();
            String messageId = request.entry().get(0).changes().get(0).value().messages().get(0).id();

            webhookService.process(request);

            assertTrue(Files.exists(tempDir.resolve(messageId + ".png")));
            verify(imageMetadataRepository, times(1)).save(any(ImageMetadataEntity.class));
        }
    }
}
