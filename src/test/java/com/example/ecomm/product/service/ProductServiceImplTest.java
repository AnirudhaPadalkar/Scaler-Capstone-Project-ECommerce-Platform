package com.example.ecomm.product.service;

import com.example.ecomm.product.exception.ProductNotFoundException;
import com.example.ecomm.product.model.Category;
import com.example.ecomm.product.model.Product;
import com.example.ecomm.product.repository.ProductRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository   productRepository;
    @Mock private ElasticsearchClient esClient;

    @InjectMocks private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .build();

        testProduct = Product.builder()
                .name("Test Phone")
                .slug("test-phone")
                .description("A great phone")
                .price(new BigDecimal("999.00"))
                .stock(10)
                .active(true)
                .category(category)
                .images(List.of())
                .build();
    }

    @Test
    void getBySlug_returnsProduct() {
        when(productRepository.findBySlugAndActiveTrue("test-phone"))
                .thenReturn(Optional.of(testProduct));

        var result = productService.getBySlug("test-phone");

        assertThat(result.getName()).isEqualTo("Test Phone");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.00"));
    }

    @Test
    void getBySlug_notFound_throwsException() {
        when(productRepository.findBySlugAndActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getBySlug("nope"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getByCategory_returnsProducts() {
        when(productRepository.findByCategoryIdAndActiveTrue("cat-1"))
                .thenReturn(List.of(testProduct));

        var results = productService.getByCategory("cat-1");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Phone");
    }
}
