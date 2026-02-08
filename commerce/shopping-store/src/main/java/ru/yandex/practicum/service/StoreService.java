package ru.yandex.practicum.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.store.enums.ProductCategory;
import ru.yandex.practicum.store.enums.ProductState;
import ru.yandex.practicum.store.enums.QuantityState;
import ru.yandex.practicum.repository.StoreRepository;
import ru.yandex.practicum.store.dto.ProductDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable) throws FeignException {
        Page<Product> products = storeRepository.findByProductCategory(productCategory, pageable);
        return products.map(productMapper::toProductDto);
    }

    @Transactional
    public ProductDto addProduct(ProductDto productDto) throws FeignException {
        Product product = productMapper.toProduct(productDto);
        Product savedProduct = storeRepository.save(product);
        return productMapper.toProductDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) throws FeignException {
        UUID productId = productDto.getProductId();
        Product product = storeRepository.findById(productId).orElse(null);
        updateProductFields(product, productDto);
        return productMapper.toProductDto(storeRepository.save(product));
    }

    @Transactional
    public boolean removeProductFromStore(UUID productId) throws FeignException {
        Product product = storeRepository.findById(productId).orElse(null);
        product.setProductState(ProductState.DEACTIVATE);
        storeRepository.save(product);
        return true;
    }

    @Transactional
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) throws FeignException {
        Product product = storeRepository.findById(productId).orElse(null);
        product.setQuantityState(quantityState);
        storeRepository.save(product);
        return true;
    }

    public ProductDto getProductById(UUID productId) throws FeignException {
        Product product = storeRepository.findById(productId).orElseThrow(() ->{
            return new RuntimeException("Product was not found");
        });
        return productMapper.toProductDto(product);
    }

    private void updateProductFields(Product product, ProductDto productDto) throws FeignException {
        if (productDto.getProductName() != null) {
            product.setProductName(productDto.getProductName());
        }
        if (productDto.getDescription() != null) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getImageSrc() != null) {
            product.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getQuantityState() != null) {
            product.setQuantityState(productDto.getQuantityState());
        }
        if (productDto.getProductState() != null) {
            product.setProductState(productDto.getProductState());
        }
        if (productDto.getProductCategory() != null) {
            product.setProductCategory(productDto.getProductCategory());
        }
        if (productDto.getPrice() != null && productDto.getPrice() >= 0) {
            product.setPrice(productDto.getPrice());
        }
    }
}