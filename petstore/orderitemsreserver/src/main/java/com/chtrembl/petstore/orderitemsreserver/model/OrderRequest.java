package com.chtrembl.petstore.orderitemsreserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Represents an order received from the PetStore application.
 * Maps to the Order model in petstoreapp.
 * The "id" field holds the session ID used for blob file naming.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

    private String id;       // session ID — used as blob file name
    private String email;
    private List<Product> products;
    private String status;
    private Boolean complete;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }
}
