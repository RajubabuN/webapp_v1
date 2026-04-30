package com.chtrembl.petstoreapp.model;

import lombok.Data;

import java.util.List;

/**
 * Pet model representing animals available in the store.
 */
@Data
public class Pet {
	private Long id;
	private Category category;
	private String name;
	private String photoURL;
	private List<Tag> tags;
	private Status status;
}
