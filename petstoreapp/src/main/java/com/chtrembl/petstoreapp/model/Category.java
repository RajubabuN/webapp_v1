package com.chtrembl.petstoreapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a product category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
	private Long id;
	private String name;
}
