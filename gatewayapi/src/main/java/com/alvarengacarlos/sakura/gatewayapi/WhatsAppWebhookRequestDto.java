package com.alvarengacarlos.sakura.gatewayapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WhatsAppWebhookRequestDto(
                @NotEmpty String object,
                @NotEmpty @Valid List<Entry> entry) {

        public record Entry(
                        @NotEmpty String id,
                        @NotEmpty @Valid List<Change> changes) {
        }

        public record Change(
                        @NotNull @Valid Value value,
                        @NotEmpty String field) {
        }

        public record Value(
                        @JsonProperty("messaging_product") @NotEmpty String messagingProduct,
                        @NotNull @Valid Metadata metadata,
                        @Valid List<Contact> contacts,
                        @Valid List<Message> messages) {
        }

        public record Metadata(
                        @JsonProperty("display_phone_number") @NotEmpty String displayPhoneNumber,
                        @JsonProperty("phone_number_id") @NotEmpty String phoneNumberId) {
        }

        public record Contact(
                        @Valid Profile profile,
                        @JsonProperty("wa_id") @NotEmpty String waId) {
        }

        public record Profile(
                        @NotEmpty String name) {
        }

        public record Message(
                        @NotEmpty String from,
                        @NotEmpty String id,
                        @NotEmpty String timestamp,
                        @NotEmpty String type,
                        @Valid ImageData image,
                        @Valid MessageContext context,
                        @Valid Referral referral) {
        }

        public record ImageData(
                        String caption,
                        @JsonProperty("mime_type") @NotEmpty String mimeType,
                        @NotEmpty String sha256,
                        @NotEmpty String id,
                        @NotEmpty String url) {
        }

        public record MessageContext(
                        boolean forwarded,
                        @JsonProperty("frequently_forwarded") boolean frequentlyForwarded) {
        }

        public record Referral(
                        @JsonProperty("source_url") String sourceUrl,
                        @JsonProperty("source_id") String sourceId,
                        @JsonProperty("source_type") String sourceType,
                        String body,
                        String headline,
                        @JsonProperty("media_type") String mediaType,
                        @JsonProperty("image_url") String imageUrl,
                        @JsonProperty("video_url") String videoUrl,
                        @JsonProperty("thumbnail_url") String thumbnailUrl,
                        @JsonProperty("ctwa_clid") String ctwaClid) {
        }
}
