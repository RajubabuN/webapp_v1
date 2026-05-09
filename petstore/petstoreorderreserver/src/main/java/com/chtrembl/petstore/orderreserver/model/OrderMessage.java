package com.chtrembl.petstore.orderreserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the order message received from Azure Service Bus.
 * Mirrors the Order model of petstoreapp (id = sessionId).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderMessage {

    /** Order / Session identifier — used as the blob file name. */
    private String id;

    /** Customer email (may be null for anonymous sessions). */
    private String email;

    /** Current order status. */
    private String status;

    /** Whether the order has been completed/checked-out. */
    private Boolean complete;

    /** Products in the cart at the time of this update. */
    private List<ProductItem> products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductItem {
        private Long id;
        private String name;
        private Integer quantity;
        @JsonProperty("photoURL")
        private String photoURL;
    }
}

