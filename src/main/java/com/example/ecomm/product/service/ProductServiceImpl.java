package com.example.ecomm.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.ecomm.product.dto.ProductResponseDto;
import com.example.ecomm.product.exception.ProductNotFoundException;
import com.example.ecomm.product.model.Product;
import com.example.ecomm.product.model.ProductDocument;
import com.example.ecomm.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository    productRepository;
    private final ElasticsearchClient  esClient;

    @Override
    public ProductResponseDto getBySlug(String slug) {
        Product product = productRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + slug));
        return toDto(product);
    }

    @Override
    public List<ProductResponseDto> getByCategory(String categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> search(String query, String categoryId) {
        try {
            SearchResponse<ProductDocument> response = esClient.search(s -> {
                var req = s.index("products")
                        .query(q -> q.bool(b -> {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("name^3", "description")
                                    .fuzziness("AUTO")
                            ));
                            b.filter(f -> f.term(t -> t.field("active").value(true)));
                            if (categoryId != null) {
                                b.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
                            }
                            return b;
                        }));
                return req;
            }, ProductDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .map(this::docToDto)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Elasticsearch search failed", e);
            return List.of();
        }
    }

    private ProductResponseDto toDto(Product p) {
        return ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .imageUrls(p.getImages().stream()
                        .map(img -> img.getUrl())
                        .collect(Collectors.toList()))
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ProductResponseDto docToDto(ProductDocument d) {
        return ProductResponseDto.builder()
                .id(d.getId())
                .name(d.getName())
                .slug(d.getSlug())
                .description(d.getDescription())
                .price(d.getPrice())
                .categoryId(d.getCategoryId())
                .categoryName(d.getCategoryName())
                .build();
    }
}
