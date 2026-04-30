package com.chtrembl.petstore.product.repository;

import com.chtrembl.petstore.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all products whose status value is in the given list
    List<Product> findByStatusIn(List<Product.Status> statuses);
}

