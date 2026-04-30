package com.chtrembl.petstore.pet.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts between Pet.Status enum and the lowercase string stored in PostgreSQL.
 * DB stores: "available" / "pending" / "sold"
 * Java enum constants: AVAILABLE / PENDING / SOLD
 */
@Converter(autoApply = true)
public class PetStatusConverter implements AttributeConverter<Pet.Status, String> {

    @Override
    public String convertToDatabaseColumn(Pet.Status status) {
        if (status == null) return null;
        return status.getValue();   // e.g. AVAILABLE → "available"
    }

    @Override
    public Pet.Status convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return Pet.Status.fromValue(dbValue);  // e.g. "available" → AVAILABLE
    }
}

