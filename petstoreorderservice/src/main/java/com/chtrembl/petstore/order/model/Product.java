package com.chtrembl.petstore.order.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product entity with quantity for orders")
public class Product {

	@NotNull(message = "Product ID cannot be null")
	@Schema(description = "Product identifier", example = "1")
	private Long id;

	@NotNull(message = "Product quantity cannot be null")
	@Max(value = 10, message = "Maximum quantity per product is 10")
	@Builder.Default
	@Schema(description = "Product quantity in order", example = "2")
	private Integer quantity = 0;

	@Size(max = 255, message = "Product name must not exceed 255 characters")
	@Schema(description = "Product name", example = "Ball")
	private String name;

	@JsonProperty("photoURL")
	@Size(max = 500, message = "Photo URL must not exceed 500 characters")
	@Schema(description = "Product photo URL",
			example = "https://raw.githubusercontent.com/chtrembl/staticcontent/master/dog-toys/ball.jpg?raw=true")
	private String photoURL;

	public Product id(Long id) {
		this.id = id;
		return this;
	}

	public Product name(String name) {
		this.name = name;
		return this;
	}

	public Integer getQuantity() {
		return quantity != null ? quantity : 0;
	}
}
