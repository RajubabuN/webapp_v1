package com.chtrembl.petstore.pet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    private Long id;
    private String name;

  public Category name(String name) {
    this.name = name;
    return this;
  }
}