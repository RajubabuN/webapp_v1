package com.chtrembl.petstore.product.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts between Product.Status enum and the lowercase string stored in PostgreSQL.
 * DB stores: "available" / "pending" / "sold"
 * Java enum constants: AVAILABLE / PENDING / SOLD
 */
@Converter(autoApply = true)
public class ProductStatusConverter implements AttributeConverter<Product.Status, String> {

    @Override
    public String convertToDatabaseColumn(Product.Status status) {
        if (status == null) return null;
        return status.getValue();   // e.g. AVAILABLE → "available"
    }

    @Override
    public Product.Status convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return Product.Status.fromValue(dbValue);  // e.g. "available" → AVAILABLE
    }
}

