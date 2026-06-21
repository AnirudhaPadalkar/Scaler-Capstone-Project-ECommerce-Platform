package com.example.ecomm.product.repository;

import com.example.ecomm.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findBySlugAndActiveTrue(String slug);
    List<Product> findByCategoryIdAndActiveTrue(String categoryId);
}
