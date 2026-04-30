package com.chtrembl.petstoreapp.model;

import lombok.Data;

import java.util.List;

/**
 * Product model representing items in the store.
 */
@Data
public class Product {
	private Long id;
	private Category category;
	private String name;
	private String photoURL;
	private List<Tag> tags;
	private Integer quantity;
}
