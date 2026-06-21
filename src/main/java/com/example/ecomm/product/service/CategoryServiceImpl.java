package com.example.ecomm.product.service;

import com.example.ecomm.product.dto.CategoryResponseDto;
import com.example.ecomm.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponseDto> listAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .collect(Collectors.toList());
    }
}
